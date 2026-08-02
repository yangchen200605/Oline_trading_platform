package com.atguigu.oline_trading_platform.service;

import com.atguigu.oline_trading_platform.entity.AddressBook;

import java.util.List;

public interface AddressBookService {

    void save(AddressBook addressBook);

    List<AddressBook> list();

    AddressBook getById(Long id);

    void update(AddressBook addressBook);

    void deleteById(Long id);

    void setDefault(AddressBook addressBook);

    AddressBook getDefault();
}
