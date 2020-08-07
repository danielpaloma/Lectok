from flask import Flask, render_template, request, redirect, url_for, flash
from flask_sqlalchemy import SQLAlchemy
import pymysql
import secrets




#connection to DB

conn = "mysql+pymysql://{0}:{1}@{2}/{3}".format(secrets.dbuser, secrets.dbpass, secrets.dbhost, secrets.dbname)

app = Flask(__name__)


app.secret_key = "Secret Key"

app.config['SQLALCHEMY_DATABASE_URI'] = conn
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False

db = SQLAlchemy(app)

# # OPTION 2: Calling the existing table 'books' of DB //reflection example
# engine = create_engine(conn)
# books_meta = MetaData(engine)
# books_table = Table('books', books_meta, autoload=True)
# DBSession = sessionmaker(bind=engine)
# session = DBSession()

#Create a Table in DB: class name should match our DB Table Name
class Books(db.Model):
	idbook = db.Column(db.Integer, primary_key=True)
	title = db.Column(db.String(200))
	author = db.Column(db.String(150))
	year = db.Column(db.Integer)
	pages_total = db.Column(db.Integer)
	pages_read = db.Column(db.Integer)
	progress = db.Column(db.Numeric(3,2))

	def __init__(self, title, author, year,pages_total,pages_read,progress):
		self.title = title
		self.author = author
		self.year = year
		self.pages_total = pages_total
		self.pages_read = pages_read
		self.progress = progress


@app.route('/')
def Index():
	books_data = Books.query.all() #variable books contains the data of DB table 'Books'
	


	return render_template('index.html', books = books_data)


@app.route('/insert', methods = ['POST'])
def insert():
	if request.method=='POST':
		title = request.form['title']
		author = request.form['author']
		year = request.form['year']
		pages_total = request.form['pages_total']
		pages_read = 0
		progress = 0

		my_data = Books(title,author,year,pages_total,pages_read,progress)
		db.session.add(my_data)
		db.session.commit()

		flash("Book inserted successfully")

		return redirect(url_for('Index'))


@app.route('/update', methods=['GET','POST'])
def update():

	if request.method == 'POST':
		#####THIS IS THE KEY LINE OF CODE!!!!
		
		my_data = Books.query.get(request.form.get('id')) #request id from EDIT FORM

		#takes the data from the EDIT FORM and updates the fields of DB row 
		my_data.title = request.form['title'] 
		my_data.Author = request.form['author']
		my_data.pages_total = request.form['pages_total']
		my_data.pages_read = request.form['pages_read']
		my_data.progress = int(my_data.pages_read) / int(my_data.pages_total)

		db.session.commit()
		flash("Progress Updated successfully")

		return redirect(url_for('Index'))


@app.route('/delete/<id>/', methods= ['GET','POST'])
def delete(id):
	my_data = Books.query.get(id) #request the id from de DB Table
	db.session.delete(my_data)
	db.session.commit()

	flash("Book deleted successfully")

	return redirect(url_for('Index'))


if __name__ == '__main__':
	app.run(debug=True)