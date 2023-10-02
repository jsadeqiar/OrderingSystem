
# Order Management System
A user-friendly Order Management System written in Java and PostgreSQL.

# Description

This program utilizes Java as a user wrapper to a Postgres Database.

Tables are constructed based off of a planned out ER diagram (picture below), and users are met with a command line interface to select what changes they wish to write to the database.

# How to build

Ensure you have the latest versions of Java and PostreSQL installed.

Create Postgres tables with ```./createPostgreDB.sh```.

Start the Database with ```startPostgreSQL.sh```.

Compile and start the program by running ```./java/scripts/compile.sh```.

NOTE: When finished and closed out of the program, stop the database with ```./stopPostgreDB.sh```.

# Media

![Diagram](https://raw.githubusercontent.com/jsadeqiar/OrderingSystem/main/ER-Diagram.png?token=GHSAT0AAAAAACGJQCRF5C2ETQFJVOSCN54WZI3GB4A)

# Authors

[@jsadeqiar](https://github.com/jsadeqiar)

