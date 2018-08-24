package com.zkjl.posite_cloud.service.impl;

import com.zkjl.posite_cloud.dao.CreditsRepository;
import com.zkjl.posite_cloud.domain.pojo.CreditsWarn;
import com.zkjl.posite_cloud.service.ICreditsConfService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author yindawei
 * @date 2018/8/14 18:41
 **/
@Service
public class CreditsConfService implements ICreditsConfService {
    @Resource
    private CreditsRepository creditsRepository;

    @Override
    public CreditsWarn save(CreditsWarn creditsWarn) {
        return creditsRepository.save(creditsWarn);
    }
}
