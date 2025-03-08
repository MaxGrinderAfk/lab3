package idespring.lab3.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;

@Entity
@Table(schema = "studentmanagement", name = "marks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Mark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int value;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "studentid")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subjectid")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
    private Subject subject;

    public Mark() {}

    public void setStudentId(Long studentId) {
        if (studentId != null) {
            this.student = new Student(studentId);
        }
    }

    public void setSubjectId(Long subjectId) {
        if (subjectId != null) {
            this.subject = new Subject();
            this.subject.setId(subjectId);
        }
    }

    public Mark(int value, Student student, Subject subject) {
        this.value = value;
        this.student = student;
        this.subject = subject;
    }

    public Mark(int value) {
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public Long getSubjectId() {
        return subject != null ? subject.getId() : null;
    }

    public String getSubjectName() {
        return subject != null ? subject.getName() : null;
    }
}