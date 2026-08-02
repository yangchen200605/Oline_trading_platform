package com.atguigu.oline_trading_platform.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("`user`")
public class User implements Serializable {

    private Long id;
    private String openid;
    private String name;
    private String phone;
    private String sex;
    private String avatar;
    private LocalDateTime createTime;
}
