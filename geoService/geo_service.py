from flask import Flask, jsonify, request, abort, Response
import psycopg2
import psycopg2.extras


app = Flask(__name__)

dbname = 'geolocation'
dbuser = 'postgres'
dbhost = 'localhost'
dbpass = 'postgres'
dbtable = 'geo.geolocation'
connection = None

@app.route("/")
def hello():
    return "Hello World!"

@app.route('/location/insideCircle', methods=['GET'])
def inside_circle():
    if 'latitude' not in request.args or 'longitude' not in request.args:
        return Response(response="Insuficient/invalid arguments", status=406)


    cursor = connectDB()

    latitude = float(request.args.get('latitude'))
    longitude = float(request.args.get('longitude'))

    query_string = """SELECT id, latitude, longitude, radius FROM %s
            WHERE ST_DWithin(ST_SetSRID(ST_MakePoint(%f,%f),4326)::geography, geom::geography, radius)
            """ % (dbtable,longitude,latitude)

    cursor.execute(query_string)
    records = cursor.fetchall()
    cursor.close()

    if records is None:
        return Response(response="Not inside circle", status=400)

    points = []
    for record in records:
        points.append({'id': record['id'], 'latitude': float(record['latitude']), 'longitude': float(record['longitude']),
                        'radius': float(record['radius'])})
    return jsonify(points)


@app.route('/location/closestPoints', methods=['GET'])
def get_closest_point():
    cursor = connectDB()

    if 'latitude' not in request.args or 'longitude' not in request.args or 'distance' not in request.args:
        return Response(response="Insuficient/invalid arguments", status=406)

    latitude = float(request.args.get('latitude'))
    longitude = float(request.args.get('longitude'))
    distance = float(request.args.get('distance'))

    if 'points' in request.args:
        chats_nr = int(request.args.get('points'))

        query_string = """SELECT id, latitude, longitude, radius, dist FROM (
                            SELECT id,latitude, longitude, radius,(ST_Distance(ST_SetSRID(ST_MakePoint(%f,%f),4326)::geography, geom::geography)) as dist
                            FROM %s
                            GROUP BY id, latitude, longitude, radius, geom
                            ORDER BY dist
                            LIMIT %d) as points
                          WHERE dist <= %f""" % (longitude,latitude,dbtable,chats_nr,distance)

    else:
        query_string = """SELECT id,latitude, longitude, radius, dist FROM (
                            SELECT id,latitude, longitude, radius,(ST_Distance(ST_SetSRID(ST_MakePoint(%f,%f),4326)::geography, geom::geography)) as dist
                            FROM %s
                            GROUP BY id, latitude, longitude, radius, geom
                            ORDER BY dist) as points
                          WHERE dist <= %f""" % (longitude, latitude, dbtable, distance)

    cursor.execute(query_string)
    records = cursor.fetchall()
    cursor.close()

    points = []

    for record in records:
        points.append({'id': record['id'], 'latitude': float(record['latitude']), 'longitude': float(record['longitude']),
                     'radius': float(record['radius']),'distance': float(record['dist'])})

    if not points:
        Response(response="No available points", status=400)

    cursor.close()

    return jsonify(points)

@app.route('/location/point', methods=['POST'])
def add_point():
    global connection
    if not request.json and (not 'latitude' in request.json \
            or not 'longitude' in request.json or not 'radius' in request.json):
        return Response(response="Insuficient/invalid arguments", status=406)

    cursor = connectDB()

    point = {
        'latitude': float(request.json['latitude']),
        'longitude': float(request.json['longitude']),
        'radius': float(request.json['radius']),
        'geom': 'ST_SetSRID(ST_MakePoint(%f,%f),4326)' % (float(request.json['longitude']),float(request.json['latitude']))
    }


    #Check if there are intersections
    #query_string = """SELECT id FROM %s
    #            WHERE ST_Intersects(ST_Buffer(ST_SetSRID(ST_MakePoint(%f,%f),4326)::geography,%f), ST_Buffer(geom::geography,radius))
    #            """ % (dbtable,chat['longitude'], chat['latitude'],chat['radius'])

    #cursor.execute(query_string)
    #records = cursor.fetchone()

    #Abort if exists intersections
    #if records is not None:
    #    return Response(response="Cannot create chat, there are intersections", status=400)

    #cursor = get_new_cursor()
    query_str = """INSERT INTO %s(latitude,longitude,radius,geom)
                VALUES (%s,%s,%s,%s)
                RETURNING id""" % (dbtable,point['latitude'],point['longitude'],point['radius'],point['geom'])

    try:
        cursor.execute(query_str)
        persist_changes()
        result = cursor.fetchone()
    except Exception as e:
        connection.rollback()
        print(e.pgerror)
        abort(405)
    print(result['id'])
    cursor.close()


    return jsonify({'id':result['id']})


@app.route('/location/point/<int:point_id>', methods=['GET'])
def get_point(point_id):
    cursor = connectDB()
    query_str = 'SELECT * FROM %s WHERE id = %d' % (dbtable, point_id)
    cursor.execute(query_str)
    results = cursor.fetchone()
    if results is None:
        cursor.close()
        return Response(response="id does not exist", status=400)

    point = {
        'id': int(results['id']),
        'latitude': float(results['latitude']),
        'longitude': float(results['longitude']),
        'radius': float(results['radius'])
    }
    cursor.close()
    return jsonify(point)


@app.route('/location/point/<int:point_id>', methods=['DELETE'])
def delete_point(point_id):
    cursor = connectDB()
    if point_exist(point_id=point_id):
        query_str = 'DELETE FROM %s WHERE id = %d' % (dbtable,point_id)
        cursor.execute(query_str)
        persist_changes()
        cursor.close()
        return Response(response="Deleted with success",status=200)
    cursor.close()
    return Response(response="id not found",status=400)

@app.route('/location/point/<int:point_id>', methods=['PUT'])
def update_point(point_id):
    if not request.json and (not 'latitude' in request.json \
            or not 'longitude' in request.json or not 'radius' in request.json):
        return Response(response="Insuficient/invalid arguments", status=406)

    cursor = connectDB()
    point = {
        'latitude': float(request.json['latitude']),
        'longitude': float(request.json['longitude']),
        'radius': float(request.json['radius']),
        'geom': 'ST_SetSRID(ST_MakePoint(%f,%f),4326)' % (float(request.json['longitude']), float(request.json['latitude']))
    }
    if point_exist(point_id):
        query_str = """UPDATE %s SET latitude = %f, longitude = %f, radius = %f, geom = %s
            WHERE id=%d;""" % (dbtable,point['latitude'],point['longitude'],point['radius'],point['geom'],point_id)

        cursor.execute(query_str)
        persist_changes()
        cursor.close()

        return Response(response="Updated with success",status=202)
    else:
        cursor.closed()
        return Response(response="id does not exist", status=400)

def point_exist(point_id):
    cursor = get_new_cursor()
    query_str = 'SELECT * FROM %s WHERE id = %d' % (dbtable,point_id)
    cursor.execute(query_str)
    if cursor.fetchone() == None:
        cursor.close()
        return False
    cursor.close()
    return True


def connectDB():
    global connection
    cursor = None
    conn_string = "host='" + dbhost + "' dbname='" + dbname + "' user='" + dbuser + "' password='" + dbpass + "'"

    try:
        if connection is None:
            connection = psycopg2.connect(conn_string)
        cursor = connection.cursor(cursor_factory=psycopg2.extras.DictCursor)

    except:
        print("Unable to connect to the database")

    return cursor

def get_new_cursor():
    global connection
    return connection.cursor(cursor_factory=psycopg2.extras.DictCursor)

def close_connection():
    global connection
    connection.close()

def persist_changes():
    global connection
    connection.commit()

if __name__ == "__main__":
    app.run(host='0.0.0.0',port=5011)
