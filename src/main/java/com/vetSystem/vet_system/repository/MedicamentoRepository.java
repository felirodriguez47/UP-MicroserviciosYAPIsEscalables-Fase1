package com.vetSystem.vet_system.repository;

import com.vetSystem.vet_system.model.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicamentoRepository extends JpaRepository<Medicamento, Long> {

    // Valida y descuenta en una sola sentencia: si fueran dos pasos (leer stock y
    // despues guardar stock - 1), dos pedidos simultaneos con stock 1 pasarian
    // los dos la validacion y el stock quedaria en -1.
    // Devuelve la cantidad de filas modificadas: 0 significa que no habia stock.
    @Modifying
    @Query("UPDATE Medicamento m SET m.stock = m.stock - 1 WHERE m.id = :id AND m.stock > 0")
    int descontarUnaUnidad(@Param("id") Long id);
}
