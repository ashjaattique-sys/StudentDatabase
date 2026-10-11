Student Database Management System
===================================

Requirements:
- IntelliJ IDEA
- JDK 17 or later
- Internet access on first run so Maven can download SQLite JDBC

How to run:
1. Extract StudentDatabase.zip.
2. In IntelliJ IDEA, choose File > Open and select the extracted StudentDatabase folder.
3. Allow IntelliJ to load/sync the Maven project.
4. Open src/main/java/Main.java.
5. Click the green Run icon beside main().

Features:
- Create SQLite database and students table automatically.
- Add student records (roll number, name, class).
- Select a table row and edit the record.
- Select a table row and delete the record after confirmation.
- Records are saved in student.db in the working directory.

Note: Roll number is the unique primary key.
