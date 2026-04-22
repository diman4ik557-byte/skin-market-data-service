package by.step.repository;

import by.step.entity.User;
import by.step.enums.UserRole;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // JPA Methods

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRole(UserRole role);

    Optional<User> findById(@NonNull Long userId);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByRegisteredAtAfter(LocalDateTime date);

    List<User> findByBalanceGreaterThan(BigDecimal amount);


    // HQL Queries

    @Query("SELECT u FROM User u WHERE u.balance > :minBalance AND u.role = :role")
    List<User> findUsersWithMinBalanceAndRole(
            @Param("minBalance") BigDecimal minBalance,
            @Param("role") UserRole role);

    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    long countByRole(@Param("role") UserRole role);

    @Query("SELECT u FROM User u WHERE u.registeredAt BETWEEN :start AND :end")
    List<User> findUsersRegisteredBetween(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);


    // Modifying Queries

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.balance = :balance WHERE u.id = :userId")
    void updateBalance(@Param("userId") Long userId,
                       @Param("balance") BigDecimal balance);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.balance = u.balance + :amount WHERE u.id = :userId")
    void addToBalance(@Param("userId") Long userId,
                      @Param("amount") BigDecimal amount);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE User u SET u.balance = u.balance - :amount WHERE u.id = :userId")
    void subtractFromBalance(@Param("userId") Long userId,
                             @Param("amount") BigDecimal amount);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.role = :role WHERE u.id = :userId")
    void updateRole(@Param("userId") Long userId,
                    @Param("role") UserRole role);

    // Native SQL Queries


    // Pagination

    Page<User> findByRole(UserRole role, Pageable pageable);

}
