package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result queryTypeList() {
        String key = SHOP_TYPE_KEY;
        // 1.从redis查询商品分类列表
        String shopTypeList = stringRedisTemplate.opsForValue().get(key);

        // 2.判断是否存在
        if (StrUtil.isNotBlank(shopTypeList)) {
            // 3.存在，直接返回
            JSONArray jsonArray = JSONUtil.parseArray(shopTypeList);
            List<ShopType> shopTypes = JSONUtil.toList(jsonArray, ShopType.class);
            // 添加缓存有效期
            //stringRedisTemplate.expire(key, SHOP_TYPE_TTL, TimeUnit.MINUTES);
            return Result.ok(shopTypes);
        }

        // 4.不存在，则从数据库查询
        List<ShopType> shopTypes = query().orderByAsc("sort").list();

        // 5.不存在，返回错误
        if (shopTypes.isEmpty()) {
            return Result.fail("商品分类列表为空");
        }

        // 6.存在，写入redis
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(shopTypes), SHOP_TYPE_TTL, TimeUnit.MINUTES);

        // 7.返回结果
        return Result.ok(shopTypes);
    }
}
