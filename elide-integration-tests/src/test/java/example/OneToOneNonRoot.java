/*
 * Copyright 2015, Yahoo Inc.
 * Licensed under the Apache License, Version 2.0
 * See LICENSE file in project root for terms.
 */
package example;

import com.yahoo.elide.annotation.CreatePermission;
import com.yahoo.elide.annotation.Include;
import com.yahoo.elide.annotation.ReadPermission;
import com.yahoo.elide.annotation.UpdatePermission;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Include(rootLevel = false)
@ReadPermission(expression = "allow all")
@CreatePermission(expression = "allow all")
@UpdatePermission(expression = "allow all")
@Entity
public class OneToOneNonRoot {
    private Long id;

    private String test;

    private OneToOneRoot root;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTest() {
        return test;
    }

    public void setTest(String test) {
        this.test = test;
    }

    @OneToOne(mappedBy = "otherObject", fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    public OneToOneRoot getRoot() {
        return root;
    }

    public void setRoot(OneToOneRoot root) {
        this.root = root;
    }
}
