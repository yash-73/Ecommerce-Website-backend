package com.ecommerce.project.repositories;

import com.ecommerce.project.model.Cart;
import com.ecommerce.project.util.AuthUtil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {


    @Query("SELECT c FROM Cart c where c.user.email = ?1")
    Cart findCartByEmail(String email);

    @Query("SELECT c FROM Cart c where c.user.email= ?1 and c.id = ?2")
    Cart findCartByEmailAndCartId(String emailId, Long cartId);

    @Modifying
    @Query("DELETE FROM Cart c where c.cartId = ?1")
    void deleteByCartId(Long cartId);
}
