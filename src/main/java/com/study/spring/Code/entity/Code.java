package com.study.spring.Code.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "code")
@Getter
@NoArgsConstructor
public class Code {

    @Id
    @Column(name = "col_id")
    private String colId;

    @Column(name = "code")
    private String code;

    @Column(name = "code_name")
    private String codeName;
}