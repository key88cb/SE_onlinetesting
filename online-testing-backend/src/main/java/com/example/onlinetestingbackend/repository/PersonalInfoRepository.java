package com.example.onlinetestingbackend.repository;

import com.example.onlinetestingbackend.entity.PersonalInfor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalInfoRepository extends JpaRepository<PersonalInfor, Integer> {

    PersonalInfor findOneByPersonalInforId(int id);
    PersonalInfor save(PersonalInfor personalInfor);
}
