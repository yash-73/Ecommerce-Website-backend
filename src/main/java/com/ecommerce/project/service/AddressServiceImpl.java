package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Address;
import com.ecommerce.project.model.User;
import com.ecommerce.project.payload.AddressDTO;
import com.ecommerce.project.repositories.AddressRepository;
import com.ecommerce.project.repositories.UserRepository;
import com.ecommerce.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;



@Service
public class AddressServiceImpl implements AddressService{

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private AuthUtil authUtil;


    @Override
    public List<AddressDTO> getAddresses(Long userId) {
        List<Address> addresses = addressRepository.findAllByUserId(userId);
        if (addresses.isEmpty()) throw new ResourceNotFoundException("Addresses", "userId", userId);
        return addresses.stream().map(address -> modelMapper.map(address, AddressDTO.class)).toList();
    }

    @Override
    public AddressDTO updateAddress(Long addressId, AddressDTO addressDTO) {
        Address savedAddress = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address", "addressId", addressId));
        savedAddress.setBuildingName(addressDTO.getBuildingName());
        savedAddress.setStreet(addressDTO.getStreet());
        savedAddress.setCity(addressDTO.getCity());
        savedAddress.setState(addressDTO.getState());
        savedAddress.setCountry(addressDTO.getCountry());
        savedAddress.setPincode(addressDTO.getPincode());

        Address updatedAddress = addressRepository.save(savedAddress);
        return modelMapper.map(updatedAddress, AddressDTO.class);


    }

    @Override
    public AddressDTO getAddress(Long addressId){
        Address userAddress = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address", "addressId", addressId));
        return modelMapper.map(userAddress, AddressDTO.class);
    }

    @Override
    public AddressDTO saveAddress(Long userId, AddressDTO addressDTO) {
        Address newAddress = modelMapper.map(addressDTO, Address.class);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "userId", userId));
        newAddress.setUser(user);

        Address savedAddress = addressRepository.save(newAddress); // This updates the relationship
        return modelMapper.map(savedAddress, AddressDTO.class);
    }


    @Override
    public void deleteAddress(Long addressId){


        Address address = addressRepository.findById(addressId)
                .orElseThrow(()-> new ResourceNotFoundException("Address", "addressId", addressId));

        User user = authUtil.loggedInUser();
        List<Address> addresses = user.getAddresses();
        addresses.remove(address);
        user.setAddresses(addresses);
        userRepository.save(user);
        addressRepository.delete(address);
    }
}
