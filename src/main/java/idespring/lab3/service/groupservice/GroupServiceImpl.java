package idespring.lab3.service.groupservice;

import idespring.lab3.model.Group;
import idespring.lab3.model.Student;
import idespring.lab3.repository.grouprepo.GroupRepository;
import idespring.lab3.repository.studentrepo.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final StudentRepository studentRepository;

    @Autowired
    public GroupServiceImpl(GroupRepository groupRepository, StudentRepository studentRepository) {
        this.groupRepository = groupRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    @Cacheable(value = "groups", key = "'allGroups' + #namePattern "
            + "+ #sort", unless = "#result == null or #result.isEmpty()")
    public List<Group> readGroups(String namePattern, String sort) {
        if (namePattern != null) {
            return groupRepository.findByNameContaining(namePattern);
        }
        if (sort != null && sort.equalsIgnoreCase("asc")) {
            return groupRepository.findAllByOrderByNameAsc();
        }
        return groupRepository.findAll();
    }

    @Override
    @Cacheable(value = "groups", key = "#id", unless = "#result == null")
    public Group findById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with id: " + id));
    }

    @Override
    @Cacheable(value = "groups", key = "'name_' + #name", unless = "#result == null")
    public Group findByName(String name) {
        return groupRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Group not found with name: "
                        + name));
    }

    @Override
    @Caching(
            put = {
                    @CachePut(value = "groups", key = "#result.id"),
                    @CachePut(value = "groups", key = "'name_' + #result.name")
            },
            evict = {
                    @CacheEvict(value = "groups", key = "'allGroups*'")
            }
    )
    @Transactional
    public Group addGroup(String name, List<Integer> studentIds) {
        Group group = new Group(name);

        if (studentIds != null && !studentIds.isEmpty()) {
            List<Student> students = studentRepository.findAllById(
                    studentIds.stream().map(Long::valueOf).toList()
            );
            for (Student student : students) {
                student.setGroup(group);
            }
            group.setStudents(students);
        }

        return groupRepository.save(group);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "groups", key = "#id"),
            @CacheEvict(value = "groups", key = "'name_' + findById(#id).name"),
            @CacheEvict(value = "groups", key = "'allGroups*'")
    })
    @Transactional
    public void deleteGroup(Long id) {
        Group group = findByIdInternal(id);
        if (group != null) {
            evictGroupByIdCache(group.getId());
        }
        groupRepository.deleteById(id);
    }

    @Caching(evict = {
            @CacheEvict(value = "groups", key = "'name_' + #name"),
            @CacheEvict(value = "groups", key = "'id_' + findByName(#name).id"),
            @CacheEvict(value = "groups", key = "'allGroups*'")
    })
    @Transactional
    public void deleteGroupByName(String name) {
        Group group = findByNameInternal(name);
        if (group != null) {
            evictGroupByNameCache(group.getName());
        }
        groupRepository.deleteByName(name);
    }

    private Group findByIdInternal(Long id) {
        return groupRepository.findById(id).orElse(null);
    }

    private Group findByNameInternal(String name) {
        return groupRepository.findByName(name).orElse(null);
    }

    @CacheEvict(value = "groups", key = "'name_' + #name")
    public void evictGroupByNameCache(String name) {
        //NEEDED METHOD
    }

    @CacheEvict(value = "groups", key = "#id")
    public void evictGroupByIdCache(Long id) {
        //NEEDED METHOD
    }
}