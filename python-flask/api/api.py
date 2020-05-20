from flask import Flask, render_template
app = Flask(__name__)

@app.route("/")
def home():
  return render_template("index.html")

@app.route("/Mohan")
def name():
  return "Hello Mohan."

#rendering the HTML page which has the button
@app.route('/json')
def json():
    return render_template('json.html')

#background process happening without any refreshing
@app.route('/background_process_test')
def background_process_test():
    return render_template("index.html")

if __name__ == "__main__":
  app.run(debug=True)
