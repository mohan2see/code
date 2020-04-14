import flask
from flask import request, jsonify

app = flask.Flask(__name__)
app.config["DEBUG"] = True

books = { "id" : 1, 
	"author": "Mohan",
	"name": "Succes"
       }, {"id" : 1,
    "author": "Murali",
	"name": "Successss"
  }


	

@app.route('/api/v1/test', methods=['GET'])
def home():
    return "<h1><<<<<<<<<<<<<<<<<<<<<<<<<<<#TESTING#>>>>>>>>>>>>>>>>>>>>>>></p>"

@app.route('/', methods=['GET'])
def home_1():
    return "<h1>Distant Reading Archive</h1><p>This site is a prototype API for distant reading of science fiction novels.</p>"


@app.route('/api/v1/test1', methods=['GET'])
def home_2():
	if 'id' in request.args:
		id = int(request.args['id'])
	print(request.args)
	if 'author' in request.args:
		author = request.args.getlist('author')
		print(author)


	results=[]
	id_list=[]
	for book in books:
		id_list.append(book['id'])
		for i in author:
			if book['id'] == id and book['author'] == i:
				results.append(book)

	if id in id_list:
		return jsonify(results)
	else:
		return "<h1 style='color:blue;'>Resource not Found</h1>"

app.run()