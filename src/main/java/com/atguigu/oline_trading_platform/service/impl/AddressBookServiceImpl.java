package com.atguigu.oline_trading_platform.service.impl;

import com.atguigu.oline_trading_platform.common.context.BaseContext;
import com.atguigu.oline_trading_platform.common.exception.BusinessException;
import com.atguigu.oline_trading_platform.entity.AddressBook;
import com.atguigu.oline_trading_platform.mapper.AddressBookMapper;
import com.atguigu.oline_trading_platform.service.AddressBookService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressBookServiceImpl implements AddressBookService {

    private final AddressBookMapper addressBookMapper;

    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public List<AddressBook> list() {
        return addressBookMapper.selectList(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId()));
    }

    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.selectById(id);
        if (addressBook == null || !BaseContext.getCurrentId().equals(addressBook.getUserId())) {
            throw new BusinessException("地址不存在");
        }
        return addressBook;
    }

    @Override
    public void update(AddressBook addressBook) {
        getById(addressBook.getId());
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        getById(id);
        addressBookMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        addressBookMapper.update(null, new LambdaUpdateWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .set(AddressBook::getIsDefault, 0));

        addressBook.setIsDefault(1);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateById(addressBook);
    }

    @Override
    public AddressBook getDefault() {
        return addressBookMapper.selectOne(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getUserId, BaseContext.getCurrentId())
                .eq(AddressBook::getIsDefault, 1));
    }
}
