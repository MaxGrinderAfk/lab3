CREATE TABLE studentManagement.groups (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE studentManagement.students (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    age INT NOT NULL,
    groupId BIGINT NOT NULL,
    CONSTRAINT fkGroup FOREIGN KEY(groupId) REFERENCES groups (id)
);

CREATE TABLE studentManagement.subjects (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE studentManagement.marks (
    id SERIAL PRIMARY KEY,
    value INT NOT NULL,
    studentId BIGINT NOT NULL,
    subjectId BIGINT NOT NULL,
    CONSTRAINT fkStudent FOREIGN KEY(studentId) REFERENCES students (id),
    CONSTRAINT fkGroups FOREIGN KEY(subjectId) REFERENCES subjects (id)
);

CREATE TABLE studentManagement.student_subject (
    studentId BIGINT NOT NULL,
    subjectId BIGINT NOT NULL,
    PRIMARY KEY (studentId, subjectId),
    CONSTRAINT fkStudentSubjectStudent FOREIGN KEY(studentId) REFERENCES students (id) ON DELETE CASCADE,
    CONSTRAINT fkStudentSubjectSubject FOREIGN KEY(subjectId) REFERENCES subjects (id)
);