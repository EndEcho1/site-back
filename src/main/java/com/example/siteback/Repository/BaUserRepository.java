package com.example.siteback.Repository;

import com.example.siteback.Entity.BaUser;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;//Optional是一个容器类，旨在解决可能出现的NullPointerException问题
@Repository
public interface BaUserRepository extends MongoRepository<BaUser, ObjectId> {
    @Query(value = "{ 'username': ?0 }", collation = "{ 'locale': 'en', 'strength': 3 }")//数字三为区分大小写的严格区分，一则不区分大小写
    Optional<BaUser> findByUsername(String username); // 根据用户名查询用户

    boolean existsByUsername(String username);//是否存在用户
    void deleteByUsername(String username);//删除用户

    @Query(value = "{ 'email': ?0 }", collation = "{ 'locale': 'en', 'strength': 3 }")//数字三为区分大小写的严格区分，一则不区分大小写
    Optional<BaUser> findByEmail(String email);

    boolean existsByEmail(String email);
    void deleteByEmail(String email);

    @Query(value = "{ 'phone_number': ?0 }", collation = "{ 'locale': 'en', 'strength': 3 }")//数字三为区分大小写的严格区分，一则不区分大小写
    Optional<BaUser> findByPhoneNumber(String phone_number);

    boolean existsByPhoneNumber(String phone_number);
    void deleteByPhoneNumber(String phone_number);


}

