package com.gymflow.model;

import java.time.LocalDateTime;

/**
 * End-user of the system who consumes workout plans and attends classes.
 */
public class Member extends User {

    public Member(long id,
                  String username,
                  String fullName,
                  String email,
                  LocalDateTime createdAt) {
        super(id, username, fullName, email, Role.MEMBER, createdAt);
    }

    @Override
    public String getRoleDisplayName() {
        return "Member";
    }
}

