package com.genc.healthins.repository;

import com.genc.healthins.model.Policy;
import com.genc.healthins.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PolicyRepository extends JpaRepository<Policy, Long> {
    List<Policy> findByUser(User user);
}
