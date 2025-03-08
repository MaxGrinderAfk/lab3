package idespring.lab3.service.groupservice;

import idespring.lab3.model.Group;
import java.util.List;

public interface GroupService {
    List<Group> readGroups(String namePattern, String sort);

    Group findById(Long id);

    Group findByName(String name);

    Group addGroup(String name, List<Integer> studentIds);

    void deleteGroup(Long id);

    void deleteGroupByName(String name);
}