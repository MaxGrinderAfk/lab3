package idespring.lab3.service.studservice;

import idespring.lab3.model.Mark;
import idespring.lab3.model.Student;
import idespring.lab3.model.Subject;
import idespring.lab3.repository.studentrepo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class StudentServiceImpl implements StudentServ {
    private final StudentRepository studentRepository;
    private static final String NOTFOUND = "Student not found with id: ";

    @Autowired
    public StudentServiceImpl(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    @Override
    @Cacheable(value = "students", key = "#age + '-' + #sort + '-' + #id",
            unless = "#result == null or #result.isEmpty()")
    public List<Student> readStudents(Integer age, String sort, Long id) {
        if (id != null) {
            return Collections.singletonList(
                    studentRepository.findById(id)
                            .orElseThrow(() -> new
                                    EntityNotFoundException(NOTFOUND + id))
            );
        }

        if (age != null && sort != null) {
            return studentRepository.findByAgeAndSortByName(age, sort);
        } else if (age != null) {
            return studentRepository.findByAge(age).stream().toList();
        } else if (sort != null) {
            return studentRepository.sortByName(sort);
        } else {
            return studentRepository.findAll();
        }
    }

    @Override
    @Cacheable(value = "students", key = "'group-' + #groupId",
            unless = "#result == null or #result.isEmpty()")
    public List<Student> findByGroupId(Long groupId) {
        return studentRepository.findByGroupId(groupId).stream().toList();
    }

    @Override
    @Cacheable(value = "students", key = "#id", unless = "#result == null")
    public Student findById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(NOTFOUND + id));
    }

    @Override
    @CachePut(value = "students", key = "#result.id")
    @CacheEvict(value = {"studentSubjects", "marks"}, allEntries = true)
    public Student addStudent(Student student) {
        Set<Long> subjectIds = student.getSubjects().stream()
                .map(Subject::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (Mark mark : student.getMarks()) {
            mark.setStudent(student);
        }

        student.setSubjects(new HashSet<>());
        Student savedStudent = studentRepository.save(student);

        for (Long subjectId : subjectIds) {
            studentRepository.addSubject(savedStudent.getId(), subjectId);
        }

        return savedStudent;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "students", key = "#id"),
            @CacheEvict(value = "students", key = "'group-' + #id")
    })
    public void updateStudent(String name, int age, long id) {
        studentRepository.update(name, age, id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "students", key = "#id"),
            @CacheEvict(value = {"marks", "studentSubjects"}, allEntries = true)
    })
    @Transactional
    public void deleteStudent(long id) {
        Student student = studentRepository.findById(id).orElseThrow();
        student.getSubjects().clear();
        studentRepository.saveAndFlush(student);
        studentRepository.delete(student);
    }
}