package bg.softuni.invoiceapplication.model;

import bg.softuni.invoiceapplication.model.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String username;                    //! Да направя проверка за дублиран username в service

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;                       //! Да направя проверка за дублиран email в service. Анотация @Email

    private String firstName;

    private String lastName;

    private String phoneNumber;

    private String address;

    private String profilePicture;

    @Column(nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdOn;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedOn;
}
