package com.jd.workflow.console.service.group;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.dao.mapper.group.RequirementInterfaceGroupMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.test.RequirementInterfaceQueryDto;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IInterfaceMethodGroupService;
import com.jd.workflow.console.service.IMethodManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class RequirementInterfaceGroupService extends ServiceImpl<RequirementInterfaceGroupMapper, RequirementInterfaceGroup>  {
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IInterfaceMethodGroupService methodGroupService;

    IMethodManageService methodManageService;
    public void excludeBigTextFiled(LambdaQueryWrapper<RequirementInterfaceGroup> lqw){
        lqw.select(RequirementInterfaceGroup.class,x->{
            String[] bigTextFields = new String[]{"sort_group_tree"};
            return Arrays.asList(bigTextFields).indexOf(x.getColumn()) == -1;
        });
    }
    public List<AppInfoDTO> getRequirementInterfaceId(Long requirementId){
        List<RequirementInterfaceGroup> interfaces = getInterfaces(requirementId, null);
        List<Long> interfaceIds = interfaces.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        return interfaceManageService.getInterfaceApps(interfaceIds);
    }
    public RequirementInterfaceGroup getByRequirementIdAndInterfaceId(Long requirementId,Long interfaceId){
        LambdaQueryWrapper<RequirementInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInterfaceGroup::getRequirementId,requirementId);
        lqw.eq(RequirementInterfaceGroup::getInterfaceId,interfaceId);
        return getOne(lqw);
    }

    public List<RequirementInterfaceGroup> getInterfaceGroups(Long id,Integer type,boolean excludeBigTextField){
        LambdaQueryWrapper<RequirementInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        if(excludeBigTextField){
            excludeBigTextFiled(lqw);
        }
        if(type != null){
            lqw.eq(RequirementInterfaceGroup::getInterfaceType,type);
        }
        lqw.eq(RequirementInterfaceGroup::getRequirementId,id);
        return list(lqw);
    }

    public List<InterfaceManage> getRequirementInterfaceList(RequirementInterfaceQueryDto dto){
        List<RequirementInterfaceGroup> entities = getInterfaces(dto.getRequirementId(),null);
        List<Long> interfaceIds = entities.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(interfaceIds);
        interfaceManageService.initInterfaceAppAndAdminInfo(interfaceManages);
        List<InterfaceManage> filteredInterfaces = interfaceManages.stream().filter(item->{
            boolean result = true;
            if(StringUtils.isNotBlank(dto.getAdminCode())){
                result&= dto.getAdminCode().equals(item.getUserCode()) || dto.getAdminCode().equals(item.getUserName());
            }
            if(dto.getType() !=null){
                result&= item.getType().equals(dto.getType());
            }
            if(dto.getAppId() != null){
                result&= dto.getAppId().equals(item.getAppId()) ;
            }

            return result;
        }).collect(Collectors.toList());
        return filteredInterfaces;
    }




    /**
     * 查询接口的树信息
     * @param interfaceId
     * @return
     */
    public RequirementInterfaceGroup findEntity(Long requirementId,Long interfaceId){
        LambdaQueryWrapper<RequirementInterfaceGroup> lqw = new LambdaQueryWrapper();
        lqw.eq(RequirementInterfaceGroup::getRequirementId,requirementId);
        lqw.eq(RequirementInterfaceGroup::getInterfaceId,interfaceId);
        List<RequirementInterfaceGroup> list = list(lqw);
        if(CollectionUtil.isEmpty(list)){
            return null;
        }else {
            return list.get(0);
        }
//        RequirementInterfaceGroup interfaceManage = getOne(lqw);
       // Guard.notEmpty(interfaceManage,"该接口分组不存在");
//        return interfaceManage;
    }

    public List<RequirementInterfaceGroup> findEntities(Long requirementId,List<Long> interfaceIds){
        LambdaQueryWrapper<RequirementInterfaceGroup> lqw = new LambdaQueryWrapper();
        lqw.eq(RequirementInterfaceGroup::getRequirementId,requirementId);
        lqw.in(RequirementInterfaceGroup::getInterfaceId,interfaceIds);


        return list(lqw);
    }


    public List<RequirementInterfaceGroup> getInterfaces(Long requirementId,Integer interfaceType){
        LambdaQueryWrapper<RequirementInterfaceGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(interfaceType != null,RequirementInterfaceGroup::getInterfaceType,interfaceType);
        lqw.eq(RequirementInterfaceGroup::getRequirementId,requirementId);
        return list(lqw);
    }
    public List<InterfaceManage> getRequirementInterfaces(Long requirementId,Integer interfaceType){
        List<RequirementInterfaceGroup> interfaces = getInterfaces(requirementId, interfaceType);
        List<Long> interfaceIds = interfaces.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        return interfaceManageService.listInterfaceByIds(interfaceIds);
    }

    public List<InterfaceManage> getRequirementInterfaces(Long requirementId,int interfaceType,String search){
        List<RequirementInterfaceGroup> interfaces = getInterfaces(requirementId, interfaceType);
        List<Long> interfaceIds = interfaces.stream().map(item -> item.getInterfaceId()).collect(Collectors.toList());
        return interfaceManageService.listInterfaceByIds(interfaceIds,search);
    }


    private List<Long> getAllMethodIds(List<RequirementInterfaceGroup> requirementInterfaces){
        return requirementInterfaces.stream().map(item->item.getSortGroupTree().allMethods())
                .flatMap(item->{
                    return item.stream();
                }).map(treeSortModel -> {
                    return treeSortModel.getId();
        }).distinct().collect(Collectors.toList());
    }


    public List<AppInfoDTO> getAppCodeByRequirementId(Long requirementId){
        return getBaseMapper().getAppCodeByRequirementId(requirementId);
    }

    /**
     * 获取空间聚合接口数
     * @param department
     * @param timeStart
     * @param timeEnd
     * @return
     */
    public Integer getRequirementInterfaceCount(String department, String timeStart, String timeEnd) {
        Integer count = 0;
        List<String> list = getBaseMapper().getRequirementInterfaceCount(department, timeStart, timeEnd);
        for (String interfaceCountArrStr : list) {
            if (StringUtils.isNotEmpty(interfaceCountArrStr)) {
                String interfaceCountStr = interfaceCountArrStr.substring(1, interfaceCountArrStr.length() - 1);
                String[] interfaceCountArr = interfaceCountStr.split(",");
                for (String interfaceCount : interfaceCountArr) {
                    count += Integer.parseInt(interfaceCount.trim());
                }
            }
        }
        return count;
    }

}
