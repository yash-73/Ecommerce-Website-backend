package com.ecommerce.project.service;

import com.ecommerce.project.payload.AddressDTO;

import java.util.List;

public interface AddressService {

    AddressDTO getAddress(Long addressId);

    List<AddressDTO> getAddresses(Long userId);

    AddressDTO updateAddress(Long addressId, AddressDTO addressDTO);

    void deleteAddress(Long addressId);

    AddressDTO saveAddress(Long userId, AddressDTO addressDTO);
}
