# NOTES : GIT
# GIT is a version control tool, helps to keep track the changes made in code.

# create a GIT repo in github / other third party providers.
# configuring github in local
git config -global user.name "mohan2see"
git config -global user.email "mohan2see@gmail.com"

# setup local repository. this code will create a folder called `code` and add `.git` folder inside.
git init code

# add a sub directory and some code
cd $HOME/code
mkdir sqoop
touch sqoop/sqoop.sh
echo "this is a test code" > sqoop/sqoop.sh

# adding repository files to index. this command will add sqoop folder and sqoop.sh inside it to local repository
git add sqoop

# committing the changes
git commit -m "sqoop initial version"

# adding the remote repo. for ssh authenication better to use git url instead of https.
git remote add origin git@github.com:mohan2see/code.git

# commiting the changes to remote repo. do a pull if you have updated work in remote before doing a push.
git push origin master

# testing git pull logic
