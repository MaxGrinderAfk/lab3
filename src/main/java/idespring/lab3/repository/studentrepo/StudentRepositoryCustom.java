package idespring.lab3.repository.studentrepo;

import idespring.lab3.model.Student;
import java.util.List;

public interface StudentRepositoryCustom {
    List<Student> findByAgeAndSortByName(int age, String sort);

    List<Student> sortByName(String sort);
}
