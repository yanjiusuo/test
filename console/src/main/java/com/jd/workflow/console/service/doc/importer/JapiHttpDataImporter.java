package com.jd.workflow.console.service.doc.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.config.dao.MetaContextHelper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.console.dto.doc.InterfaceDocConfig;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.entity.sync.DataSyncRecord;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.console.service.doc.importer.dto.*;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.remote.EasyMockRemoteService;
import com.jd.workflow.console.service.sync.DataSyncRecordService;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.y.model.dto.HttpDataDto;
import com.jd.y.model.vo.HttpDataVO;
import com.jd.y.response.ReplyVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 该功能用来导入japi的接口数据到当前平台，几个注意事项：
 * 1. j-api的数据与当前记录映射通过relatedId关联
 * 2. 只在在j-api侧新增、修改、删除的数据需要通过到j-api
 * 3. 如果数据在j-api以及在线联调都修改了，以修改时间最新的为准
 */
@Service
@Slf4j
public class JapiHttpDataImporter {
    //static String JAPI_BASE_URL = "http://j-api.jd.com";
    // static String JAPI_BASE_URL = "http://wjf.jd.com:8010";
    static String JAPI_BASE_URL = "http://ams.jd.care";
    public static String JAPI_APP_PREFIX = "japi_";
    static String JAPI_INTERFACE_DEFAULT_APP = "japiDefault";
    static String PROJECT_URL  = "/Project/getProjectList";
    public final static String unicodeReg= "["+
            "\u4E00-\u9FBF"+//：CJK 统一表意符号 (CJK Unified Ideographs)
            "\u4DC0-\u4DFF"+//：易经六十四卦符号 (Yijing Hexagrams Symbols)
            "\u0000-\u007F"+//：C0控制符及基本拉丁文 (C0 Control and Basic Latin)
            "\u0080-\u00FF"+//：C1控制符及拉丁：补充-1 (C1 Control and Latin 1 Supplement)
            "\u0100-\u017F"+//：拉丁文扩展-A (Latin Extended-A)
            "\u0180-\u024F"+//：拉丁文扩展-B (Latin Extended-B)
            "\u0250-\u02AF"+//：国际音标扩展 (IPA Extensions)
            "\u02B0-\u02FF"+//：空白修饰字母 (Spacing Modifiers)
            "\u0300-\u036F"+//：结合用读音符号 (Combining Diacritics Marks)
            "\u0370-\u03FF"+//：希腊文及科普特文 (Greek and Coptic)
            "\u0400-\u04FF"+//：西里尔字母 (Cyrillic)
            "\u0500-\u052F"+//：西里尔字母补充 (Cyrillic Supplement)
            "\u0530-\u058F"+//：亚美尼亚语 (Armenian)
            "\u0590-\u05FF"+//：希伯来文 (Hebrew)
            "\u0600-\u06FF"+//：阿拉伯文 (Arabic)
            "\u0700-\u074F"+//：叙利亚文 (Syriac)
            "\u0750-\u077F"+//：阿拉伯文补充 (Arabic Supplement)
            "\u0780-\u07BF"+//：马尔代夫语 (Thaana)
            //"\u07C0-\u077F"+//：西非书面语言 (N'Ko)
            "\u0800-\u085F"+//：阿维斯塔语及巴列维语 (Avestan and Pahlavi)
            "\u0860-\u087F"+//：Mandaic
            "\u0880-\u08AF"+//：撒马利亚语 (Samaritan)
            "\u0900-\u097F"+//：天城文书 (Devanagari)
            "\u0980-\u09FF"+//：孟加拉语 (Bengali)
            "\u0A00-\u0A7F"+//：锡克教文 (Gurmukhi)
            "\u0A80-\u0AFF"+//：古吉拉特文 (Gujarati)
            "\u0B00-\u0B7F"+//：奥里亚文 (Oriya)
            "\u0B80-\u0BFF"+//：泰米尔文 (Tamil)
            "\u0C00-\u0C7F"+//：泰卢固文 (Telugu)
            "\u0C80-\u0CFF"+//：卡纳达文 (Kannada)
            "\u0D00-\u0D7F"+//：德拉维族语 (Malayalam)
            "\u0D80-\u0DFF"+//：僧伽罗语 (Sinhala)
            "\u0E00-\u0E7F"+//：泰文 (Thai)
            "\u0E80-\u0EFF"+//：老挝文 (Lao)
            "\u0F00-\u0FFF"+//：藏文 (Tibetan)
            "\u1000-\u109F"+//：缅甸语 (Myanmar)
            "\u10A0-\u10FF"+//：格鲁吉亚语 (Georgian)
            "\u1100-\u11FF"+//：朝鲜文 (Hangul Jamo)
            "\u1200-\u137F"+//：埃塞俄比亚语 (Ethiopic)
            "\u1380-\u139F"+//：埃塞俄比亚语补充 (Ethiopic Supplement)
            "\u13A0-\u13FF"+//：切罗基语 (Cherokee)
            "\u1400-\u167F"+//：统一加拿大土著语音节 (Unified Canadian Aboriginal Syllabics)
            "\u1680-\u169F"+//：欧甘字母 (Ogham)
            "\u16A0-\u16FF"+//：如尼文 (Runic)
            "\u1700-\u171F"+//：塔加拉语 (Tagalog)
            "\u1720-\u173F"+//：Hanunóo
            "\u1740-\u175F"+//：Buhid
            "\u1760-\u177F"+//：Tagbanwa
            "\u1780-\u17FF"+//：高棉语 (Khmer)
            "\u1800-\u18AF"+//：蒙古文 (Mongolian)
            "\u18B0-\u18FF"+//：Cham
            "\u1900-\u194F"+//：Limbu
            "\u1950-\u197F"+//：德宏泰语 (Tai Le)
            "\u1980-\u19DF"+//：新傣仂语 (New Tai Lue)
            "\u19E0-\u19FF"+//：高棉语记号 (Kmer Symbols)
            "\u1A00-\u1A1F"+//：Buginese
            "\u1A20-\u1A5F"+//：Batak
            "\u1A80-\u1AEF"+//：Lanna
            "\u1B00-\u1B7F"+//：巴厘语 (Balinese)
            "\u1B80-\u1BB0"+//：巽他语 (Sundanese)
            "\u1BC0-\u1BFF"+//：Pahawh Hmong
            "\u1C00-\u1C4F"+//：雷布查语(Lepcha)
            "\u1C50-\u1C7F"+//：Ol Chiki
            "\u1C80-\u1CDF"+//：曼尼普尔语 (Meithei/Manipuri)
            "\u1D00-\u1D7F"+//：语音学扩展 (Phone tic Extensions)
            "\u1D80-\u1DBF"+//：语音学扩展补充 (Phonetic Extensions Supplement)
            "\u1DC0-\u1DFF"+//结合用读音符号补充 (Combining Diacritics Marks Supplement)
            "\u1E00-\u1EFF"+//：拉丁文扩充附加 (Latin Extended Additional)
            "\u1F00-\u1FFF"+//：希腊语扩充 (Greek Extended)
            "\u2000-\u206F"+//：常用标点 (General Punctuation)
            "\u2070-\u209F"+//：上标及下标 (Superscripts and Subscripts)
            "\u20A0-\u20CF"+//：货币符号 (Currency Symbols)
            "\u20D0-\u20FF"+//：组合用记号 (Combining Diacritics Marks for Symbols)
            "\u2100-\u214F"+//：字母式符号 (Letterlike Symbols)
            "\u2150-\u218F"+//：数字形式 (Number Form)
            "\u2190-\u21FF"+//：箭头 (Arrows)
            "\u2200-\u22FF"+//：数学运算符 (Mathematical Operator)
            "\u2300-\u23FF"+//：杂项工业符号 (Miscellaneous Technical)
            "\u2400-\u243F"+//：控制图片 (Control Pictures)
            "\u2440-\u245F"+//：光学识别符 (Optical Character Recognition)
            "\u2460-\u24FF"+//：封闭式字母数字 (Enclosed Alphanumerics)
            "\u2500-\u257F"+//：制表符 (Box Drawing)
            "\u2580-\u259F"+//：方块元素 (Block Element)
            "\u25A0-\u25FF"+//：几何图形 (Geometric Shapes)
            "\u2600-\u26FF"+//：杂项符号 (Miscellaneous Symbols)
            "\u2700-\u27BF"+//：印刷符号 (Dingbats)
            "\u27C0-\u27EF"+//：杂项数学符号-A (Miscellaneous Mathematical Symbols-A)
            "\u27F0-\u27FF"+//：追加箭头-A (Supplemental Arrows-A)
            "\u2800-\u28FF"+//：盲文点字模型 (Braille Patterns)
            "\u2900-\u297F"+//：追加箭头-B (Supplemental Arrows-B)
            "\u2980-\u29FF"+//：杂项数学符号-B (Miscellaneous Mathematical Symbols-B)
            "\u2A00-\u2AFF"+//：追加数学运算符 (Supplemental Mathematical Operator)
            "\u2B00-\u2BFF"+//：杂项符号和箭头 (Miscellaneous Symbols and Arrows)
            "\u2C00-\u2C5F"+//：格拉哥里字母 (Glagolitic)
            "\u2C60-\u2C7F"+//：拉丁文扩展-C (Latin Extended-C)
            "\u2C80-\u2CFF"+//：古埃及语 (Coptic)
            "\u2D00-\u2D2F"+//：格鲁吉亚语补充 (Georgian Supplement)
            "\u2D30-\u2D7F"+//：提非纳文 (Tifinagh)
            "\u2D80-\u2DDF"+//：埃塞俄比亚语扩展 (Ethiopic Extended)
            "\u2E00-\u2E7F"+//：追加标点 (Supplemental Punctuation)
            "\u2E80-\u2EFF"+//：CJK 部首补充 (CJK Radicals Supplement)
            "\u2F00-\u2FDF"+//：康熙字典部首 (Kangxi Radicals)
            "\u2FF0-\u2FFF"+//：表意文字描述符 (Ideographic Description Characters)
            "\u3000-\u303F"+//：CJK 符号和标点 (CJK Symbols and Punctuation)
            "\u3040-\u309F"+//：日文平假名 (Hiragana)
            "\u30A0-\u30FF"+//：日文片假名 (Katakana)
            "\u3100-\u312F"+//：注音字母 (Bopomofo)
            "\u3130-\u318F"+//：朝鲜文兼容字母 (Hangul Compatibility Jamo)
            "\u3190-\u319F"+//：象形字注释标志 (Kanbun)
            "\u31A0-\u31BF"+//：注音字母扩展 (Bopomofo Extended)
            "\u31C0-\u31EF"+//：CJK 笔画 (CJK Strokes)
            "\u31F0-\u31FF"+//：日文片假名语音扩展 (Katakana Phonetic Extensions)
            "\u3200-\u32FF"+//：封闭式 CJK 文字和月份 (Enclosed CJK Letters and Months)
            "\u3300-\u33FF"+//：CJK 兼容 (CJK Compatibility)
            "\u3400-\u4DBF"+//：CJK 统一表意符号扩展 A (CJK Unified Ideographs Extension A)
            "\u4DC0-\u4DFF"+//：易经六十四卦符号 (Yijing Hexagrams Symbols)
            "\u4E00-\u9FBF"+//：CJK 统一表意符号 (CJK Unified Ideographs)
            "\uA000-\uA48F"+//：彝文音节 (Yi Syllables)
            "\uA490-\uA4CF"+//：彝文字根 (Yi Radicals)
            "\uA500-\uA61F"+//：Vai
            "\uA660-\uA6FF"+//：统一加拿大土著语音节补充 (Unified Canadian Aboriginal Syllabics Supplement)
            "\uA700-\uA71F"+//：声调修饰字母 (Modifier Tone Letters)
            "\uA720-\uA7FF"+//：拉丁文扩展-D (Latin Extended-D)
            "\uA800-\uA82F"+//：Syloti Nagri
            "\uA840-\uA87F"+//：八思巴字 (Phags-pa)
            "\uA880-\uA8DF"+//：Saurashtra
            "\uA900-\uA97F"+//：爪哇语 (Javanese)
            "\uA980-\uA9DF"+//：Chakma
            "\uAA00-\uAA3F"+//：Varang Kshiti
            "\uAA40-\uAA6F"+//：Sorang Sompeng
            "\uAA80-\uAADF"+//：Newari
            "\uAB00-\uAB5F"+//：越南傣语 (Vi?t Thái)
            "\uAB80-\uABA0"+//：Kayah Li
            "\uAC00-\uD7AF"+//：朝鲜文音节 (Hangul Syllables)
            //"\uD800-\uDBFF"+//：High-half zone of UTF-16
            //"\uDC00-\uDFFF"+//：Low-half zone of UTF-16
            "\uE000-\uF8FF"+//：自行使用区域 (Private Use Zone)
            "\uF900-\uFAFF"+//：CJK 兼容象形文字 (CJK Compatibility Ideographs)
            "\uFB00-\uFB4F"+//：字母表达形式 (Alphabetic Presentation Form)
            "\uFB50-\uFDFF"+//：阿拉伯表达形式A (Arabic Presentation Form-A)
            "\uFE00-\uFE0F"+//：变量选择符 (Variation Selector)
            "\uFE10-\uFE1F"+//：竖排形式 (Vertical Forms)
            "\uFE20-\uFE2F"+//：组合用半符号 (Combining Half Marks)
            "\uFE30-\uFE4F"+//：CJK 兼容形式 (CJK Compatibility Forms)
            "\uFE50-\uFE6F"+//：小型变体形式 (Small Form Variants)
            "\uFE70-\uFEFF"+//：阿拉伯表达形式B (Arabic Presentation Form-B)
            "\uFF00-\uFFEF"+//：半型及全型形式 (Halfwidth and Fullwidth Form)
            "\uFFF0-\uFFFF]";//：特殊 (Specials);
    @Autowired
    ConfigInfoService configInfoService;
    @Autowired
    CjgHelper cjgHelper;
    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;
    @Autowired
    DataSyncRecordService dataSyncService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    private IAppInfoMembersService appInfoMembersService;
    @Autowired
    IMemberRelationService memberRelationService;
    @Autowired
    private EasyMockRemoteService testEasyMockRemoteService;

    @Autowired
    JapiDataSyncService japiDataSyncService;

    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IInterfaceVersionService versionService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    DataSyncRecordService dataSyncRecordService;
    @Autowired
    IInterfaceMethodGroupService interfaceMethodGroupService;


    @Autowired
    IInterfaceVersionService interfaceVersionService;

    private RequestClient getClient(String cookie){
        Map<String,Object> headers = new HashMap<>();
        headers.put("Cookie","sso.jd.com="+cookie);
        RequestClient requestClient = new RequestClient(JAPI_BASE_URL,headers);
        return requestClient;
    }

    public JApiProjectResult getJApiProjectList(String cookie, int page, int pageSize){
        RequestClient client = getClient(cookie);
        Map<String,Object> params = new HashMap<>();
        params.put("page",page);
        params.put("pageSize",pageSize);
        String result = client.get("/external/getProjectIncludePartner", params);
        JApiProjectResult projectResult = JsonUtils.parse(result, JApiProjectResult.class);
        return projectResult;
    }
    public void initJApiApp(boolean forceUpdateApp){
        final JApiProjectResult jddjResult = getJApiProjectList(null, 1,1);
         int count  = jddjResult.getProjectListCount();
         int pageSize = 100;
        for (int i = 1; i <= count/pageSize+2; i++) {
            final int page = i;
            final UserInfoInSession user = UserSessionLocal.getUser();
            scheduleService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        MetaContextHelper.skipModify(true);
                        UserSessionLocal.setUser(user);
                        syncJapiApp(null,page,pageSize,forceUpdateApp);
                        MetaContextHelper.clearModifyState();
                    }catch (Exception e){
                        log.error("japi.err_sync_app",e);
                    }finally {
                        UserSessionLocal.removeUser();
                    }
                }
            });

        }
    }
    public List<JApiProjectInfo> getAllProject(){
        final JApiProjectResult jddjResult = getJApiProjectList(null, 1,1);
        int count  = jddjResult.getProjectListCount();
        int pageSize = 100;
        List<JApiProjectInfo> result = new ArrayList<>();
        for (int i = 1; i <= count/pageSize+2; i++) {
            final int page = i;
            JApiProjectResult projects = getJApiProjectList(null, page,pageSize);
             List<JApiProjectInfo> projectList = projects.getProjectList();
            result.addAll(projectList);
        }
        return result;
    }

    public void cleanDjImportedData(){
        List<Long> interfaceIds = getDjInterfaces();
        dataSyncService.clearSyncRecord("japi");
        clearImportData(interfaceIds);
    }

    public List<Long> getDjInterfaces(){
        List<AppInfo> djApps = appInfoService.queryDjAppByPrefix(JAPI_APP_PREFIX);
        List<Long> appIds = djApps.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getAppId,appIds);

        List<InterfaceManage> interfaces = interfaceManageService.list(lqw);
        return interfaces.stream().map(vs->vs.getId()).collect(Collectors.toList());
    }

    public void clearImportData(List<Long> interfaceIds){
        //List<MethodManage> interfaceMethods = methodManageService.getInterfaceMethods(interfaceIds);

        LambdaQueryWrapper<MethodManage> methodLqw = new LambdaQueryWrapper<>();
        methodLqw.in(MethodManage::getInterfaceId,interfaceIds);
        methodManageService.remove(methodLqw);

        LambdaQueryWrapper<MethodModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodModifyLog::getInterfaceId,interfaceIds);
       // methodModifyLogService.remove(modifyLog);

        LambdaQueryWrapper<MethodVersionModifyLog> modifyVersionLog = new LambdaQueryWrapper<>();
        modifyVersionLog.in(MethodVersionModifyLog::getInterfaceId,interfaceIds);
        //methodVersionModifyLogService.remove(modifyVersionLog);
        interfaceManageService.removeByIds(interfaceIds);

        LambdaQueryWrapper<InterfaceVersion> interfaceVersion = new LambdaQueryWrapper<>();
        interfaceVersion.in(InterfaceVersion::getInterfaceId,interfaceIds);
        versionService.remove(interfaceVersion);
    }
    /**
     *  成员权限：
     *  0-创建者 1-管理员 2-普通成员 3-只读普通成员  4-申请中 99999-无权限
     * @param cookie
     * @param page
     * @param pageSize
     */
    public void syncJapiApp(String cookie,int page,int pageSize,boolean forceUpdateApp){
        final JApiProjectResult jddjResult = getJApiProjectList(cookie, page,pageSize);
       /* if(!jddjResult.isSuccess()){
            log.error("jddj.err_get_app:result={}",JsonUtils.toJSONString(jddjResult));
            return;
        }*/

        List<AppInfo> appInfos = appInfoService.queryDjAppByPrefix(JAPI_APP_PREFIX);
        Map<String, List<AppInfo>> appCode2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getAppCode));

        for (JApiProjectInfo jApiProjectInfo : jddjResult.getProjectList()) {
            try{
                initJApiApp(jApiProjectInfo,appCode2Apps,forceUpdateApp);
            }catch (Exception e){
                log.error("app.err_init_app:appCode={},name={}",jApiProjectInfo.getProjectID(),jApiProjectInfo.getProjectName(),e);
            }

        }
    }

    public List<InterfaceManage> getJapiInterfaces(){
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(InterfaceManage::getRelatedId);
        lqw.eq(InterfaceManage::getYn,1);
        List<InterfaceManage> interfaces = interfaceManageService.list(lqw);
        return interfaces;
    }
    public void initJapiInterfaces(Long japiId){
        final JApiProjectResult jddjResult = getJApiProjectList(null, 1,1);
        int count  = jddjResult.getProjectListCount();
        int pageSize = 100;
        List<InterfaceManage> japiInterfaces = getJapiInterfaces();
         Map<Long, List<InterfaceManage>> japiId2InterfaceManages = japiInterfaces.stream().collect(Collectors.groupingBy(InterfaceManage::getRelatedId));


        for (int i = 1; i <= count/pageSize+2; i++) {
            final int page = i;
            final UserInfoInSession user = UserSessionLocal.getUser();
            scheduleService.execute(new Runnable() {
                @Override
                public void run() {
                    try{
                        MetaContextHelper.skipModify(true);
                        UserSessionLocal.setUser(user);
                        if(japiId == null){
                            syncJapiInterfaces(japiId2InterfaceManages,null,page,pageSize);
                        }else{
                            syncJapiInterfaces(japiId2InterfaceManages,Collections.singletonList(japiId),page,pageSize);
                        }

                        MetaContextHelper.clearModifyState();
                    }catch (Exception e){
                        log.error("japi.err_sync_app",e);
                    }
                }
            });

        }
    }
    public void initJapiAppInterfaces(){
        List<InterfaceManage> japiInterfaces = getJapiInterfaces();
        for (InterfaceManage japiInterface : japiInterfaces) {
            try{
                syncProjectMembersToApp(japiInterface);
            }catch (Exception e){
                log.error("japi.err_sync_members_to_app:appId={},appCode={}",e);
            }

        }
    }
    public void syncJapiTemplateDataToLocal(){

    }


    public void syncJapiInterfaces(Map<Long, List<InterfaceManage>> japiId2InterfaceManages ,List<Long> needSyncIds,int page,int pageSize){
        final JApiProjectResult jddjResult = getJApiProjectList(null, page,pageSize);

        for (JApiProjectInfo jApiProjectInfo : jddjResult.getProjectList()) {
            if(!ObjectHelper.isEmpty(needSyncIds) && !needSyncIds.contains(jApiProjectInfo.getProjectID())){
                continue;
            }
            try{
                final List<InterfaceManage> interfaceManages = japiId2InterfaceManages.get(jApiProjectInfo.getProjectID());
                InterfaceManage interfaceManage = null;
                if(interfaceManages!=null){
                    interfaceManage = interfaceManages.get(0);
                }
                initJApiInterface(jApiProjectInfo,interfaceManage,false);
            }catch (Exception e){
                log.error("app.err_init_app:appCode={},name={}",jApiProjectInfo.getProjectID(),jApiProjectInfo.getProjectName(),e);
            }

        }
    }


    public Long initJapiProjectInterface(Long projectId){
        JApiProjectInfo projectInfo = getProjectInfoIncludePartner(projectId);
        InterfaceManage interfaceManage = newInterfaceManage(projectInfo);
        interfaceManageService.save(interfaceManage);
        return interfaceManage.getId();
    }
    public JApiProjectInfo getProjectInfoIncludePartner(Long projectId){
        RequestClient client = new RequestClient(JAPI_BASE_URL,null);
        Map<String,Object> params = new HashMap<>();
        params.put("projectID",projectId);
        String result = client.post("/external/getProjectInfoIncludePartnerNoAuth", params,null);
        Map map = JsonUtils.parse(result, Map.class);
        Map projectInfoMap = (Map) map.get("projectInfo");
        JApiProjectInfo jApiProjectInfo = JsonUtils.cast(projectInfoMap, JApiProjectInfo.class);
        return jApiProjectInfo;
    }
    public JApiProjectInfo getProject(Long projectId){
        RequestClient client = new RequestClient(JAPI_BASE_URL,null);
        Map<String,Object> params = new HashMap<>();
        params.put("projectID",projectId);
         String result = client.post("/external/getProjectNoAuth", params,null);
         Map map = JsonUtils.parse(result, Map.class);
         Map projectInfoMap = (Map) map.get("projectInfo");
         JApiProjectInfo jApiProjectInfo = JsonUtils.cast(projectInfoMap, JApiProjectInfo.class);
         return jApiProjectInfo;
    }
    public List<JApiProjectOwner> getUserList(){
        RequestClient client = new RequestClient(JAPI_BASE_URL,null);
        Map<String,Object> params = new HashMap<>();
        String result = client.post("/external/getAllUserNoAuth", params,null);
        List<JApiProjectOwner> owners = JsonUtils.parse(result, new TypeReference<List<JApiProjectOwner>>() {

        });
        return owners;
    }
    private boolean isSameTime(Long time1,Long time2){
        if(time1 == null || time2== null) return false;
        return Math.abs(time1-time2) <= 1000L;
    }
    private boolean isAfter(Long time1,Long time2){
        if(isSameTime(time1,time2)){
           return false;
        }
        return time1 > time2;
    }
    private void initJApiApp(JApiProjectInfo jApiProjectInfo,Map<String, List<AppInfo>> appCode2Apps,boolean forceSyncApp){
        String appCode = JAPI_APP_PREFIX + jApiProjectInfo.getProjectID();
        List<AppInfo> japiApps = appCode2Apps.get(appCode);
        Long id = null;
        Long oldModifyTime = Long.valueOf(jApiProjectInfo.getProjectUpdateTime());
        if(japiApps != null){
            Long modified = japiApps.get(0).getModified().getTime();
            if( isAfter(modified,oldModifyTime) && !forceSyncApp
            ){
                log.info("app.skip_modify_app:appCode={}",appCode);
                return;
            }
            id = japiApps.get(0).getId();
        }

        AppInfoDTO dto = new AppInfoDTO();
        dto.setId(id);
        dto.setAppCode(appCode );
        if(jApiProjectInfo.getProjectCreateTime() != null){
            dto.setCreated(new Date(Long.valueOf(jApiProjectInfo.getProjectCreateTime())));
        }
        if(jApiProjectInfo.getProjectUpdateTime() != null){
            dto.setModified(new Date(oldModifyTime));
        }

        dto.setAppName(jApiProjectInfo.getProjectName());
        dto.setAuthLevel("0");
        String master = "";
        if(jApiProjectInfo.getPartners() != null && !jApiProjectInfo.getPartners().isEmpty()){
            Optional<JApiProjectOwner> masterPartner = jApiProjectInfo.getPartners().stream().filter(partner -> {
                return partner.getUserType() != null && partner.getUserType() <= 1;
            }).findAny();
            if(masterPartner.isPresent()){
                master = masterPartner.get().getUserName();
            }else{
                master = jApiProjectInfo.getPartners().get(0).getUserName();
            }

        }
        final String master1 = master;
        List<String> members =  new ArrayList<>();
        if(jApiProjectInfo.getPartners() != null && !jApiProjectInfo.getPartners().isEmpty()){
            members = jApiProjectInfo.getPartners().stream()
                    .filter(vs -> !master1.equals(vs.getUserName()))
                    .filter(vs -> vs.getUserType() != null && vs.getUserType() <= 3).map(vs->vs.getUserName()).collect(Collectors.toList());
        }
        /*if(!members.contains("wangjingfang3")){
            members.add("wangjingfang3");
        }*/

        dto.setOwner(Collections.singletonList(master));
        dto.setMember(members);

        dto.setTenantId(configInfoService.getFixTenantId());
      //  dto.setMember(Collections.singletonList(master));
        dto.setTester(Collections.singletonList(master));
        dto.setProductor(Collections.singletonList(master));
        AppInfoDTO cjgApp = cjgHelper.getCjgComponetInfoByCode(dto.getAppCode());
        if(cjgApp != null){
            dto.setCjgAppId(cjgApp.getAppCode());
        }
        if(id == null){
            appInfoService.addApp(dto);
        }else{
            appInfoService.modifyApp(dto,true);
        }
    }
    private List<InterfaceManage> getAppInterfaces(List<AppInfo> appInfos){
         List<Long> appIds = appInfos.stream().map(vs -> vs.getId()).collect(Collectors.toList());
         LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
         lqw.in(InterfaceManage::getAppId,appIds);
         lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
         lqw.eq(InterfaceManage::getServiceCode,JAPI_INTERFACE_DEFAULT_APP);
         List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
         return interfaceManages;
    }
    private Map<Long/*japiGroupId*/,Long/* 在线联调groupId */> mergeApiGroup(JApiGroupSortTree sortTree,Long interfaceId){

        Map<Long, InterfaceMethodGroup> interfaceGroups = interfaceMethodGroupService.findInterfaceGroups(interfaceId);
        LambdaQueryWrapper<InterfaceMethodGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceMethodGroup::getInterfaceId,interfaceId);
        interfaceMethodGroupService.remove(lqw);

        List<InterfaceMethodGroup> groups = new ArrayList<>();
        for (JApiGroupSortTree.ApiGroup apiGroup : sortTree.getApiGroupArrForSort()) {
            InterfaceMethodGroup interfaceMethodGroup  = new InterfaceMethodGroup();
            interfaceMethodGroup.setInterfaceId(interfaceId);
            interfaceMethodGroup.setType(GroupTypeEnum.APP.getCode());
            interfaceMethodGroup.setName(apiGroup.getGroupName());
            interfaceMethodGroup.setRelatedId(apiGroup.getGroupID());
            groups.add(interfaceMethodGroup);
        }
        interfaceMethodGroupService.saveBatch(groups);
        Map<Long,Long> ret = new HashMap<>();
        for (InterfaceMethodGroup group : groups) {
            ret.put(group.getRelatedId(),group.getId());
        }
        return ret;
    }
    private Map<Long,List<MethodManage>> groupMethodsByRelatedId(List<MethodManage> methods){
        Map<Long,List<MethodManage>> ret = new HashMap<>();
        for (MethodManage method : methods) {
            List<MethodManage> methodManages = ret.computeIfAbsent(method.getRelatedId(), key -> {
                return new ArrayList<>();
            });
            methodManages.add(method);
        }
        return ret;
    }
    public InterfaceManage newInterfaceManage(JApiProjectInfo projectInfo){
        InterfaceManage manage = new InterfaceManage();
        manage.setTenantId(configInfoService.getFixTenantId());
        manage.setAppId(null);
        manage.setCreated(new Date());
        manage.setModified(new Date());
        manage.setYn(DataYnEnum.VALID.getCode());
        manage.setName(projectInfo.getProjectName());
        manage.setRelatedId(projectInfo.getProjectID());
        InterfaceDocConfig docConfig = new InterfaceDocConfig();
        docConfig.setDocType("md");
        manage.setDocConfig(docConfig);
        manage.setServiceCode("japiDefault");
        //manage.setGroupLastVersion(sortTree.getSortTreeVersion()+"");
        //manage.setSortGroupTree(sortTree.toGroupTreeModel());
        manage.setType(InterfaceTypeEnum.HTTP.getCode());
        manage.setIsPublic(0);
        manage.setVisibility(0);
        return manage;
    }
    private void initJApiInterface(JApiProjectInfo projectInfo,InterfaceManage manage,boolean forceUpdate){

            log.info("japi.begin_sync_interface:projectId={},name={},id={},manage={}",projectInfo.getProjectID(),projectInfo.getProjectName(),manage!=null?manage.getId():null,manage);

            if(manage == null){
                manage = new InterfaceManage();
                manage.setTenantId(configInfoService.getFixTenantId());
                manage.setAppId(null);
                manage.setCreated(new Date());
                manage.setModified(new Date());
                manage.setYn(DataYnEnum.VALID.getCode());
                manage.setName(projectInfo.getProjectName());
                manage.setRelatedId(projectInfo.getProjectID());
                InterfaceDocConfig docConfig = new InterfaceDocConfig();
                docConfig.setDocType("md");
                manage.setDocConfig(docConfig);
                manage.setServiceCode("japi_"+projectInfo.getProjectID());
                //manage.setGroupLastVersion(sortTree.getSortTreeVersion()+"");
                //manage.setSortGroupTree(sortTree.toGroupTreeModel());
                manage.setType(InterfaceTypeEnum.HTTP.getCode());
                manage.setIsPublic(0);
                manage.setVisibility(0);
                interfaceManageService.save(manage);
                interfaceVersionService.initInterfaceVersion(manage);


            }else{

                InterfaceDocConfig docConfig = new InterfaceDocConfig();
                docConfig.setDocType("md");
                manage.setDocConfig(docConfig);
                manage.setIsPublic(0);
                manage.setVisibility(0);
                interfaceManageService.updateById(manage);

            }

            japiDataSyncService.syncJapiInterface(manage,forceUpdate);
    }
    public void initInterfaceProjectMembers(Long interfaceId){
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        syncProjectMembersToApp(interfaceManage);
    }
    public void syncProjectMembersToApp(InterfaceManage interfaceManage){
        if(interfaceManage.getRelatedId() == null) return ;
        JApiProjectInfo projectInfo = getProjectInfoIncludePartner(interfaceManage.getRelatedId());
        if(interfaceManage.getAppId() == null) return;
        AppInfo appInfo = appInfoService.getById(interfaceManage.getAppId());
        AppInfoDTO dto = new AppInfoDTO();
        List<String> members = dto.splitMembers(appInfo.getMembers());
        for (JApiProjectOwner partner : projectInfo.getPartners()) {
            MemberRelation relation = partner.toRelation();
            if(relation == null){
                continue;
            }
            String userCode = relation.getUserCode();
            if(dto.getMember() == null){
                dto.setMember(new ArrayList<>());
            }
            if(!members.contains(userCode)){
                dto.getMember().add(relation.getUserCode());
            }


        }
        log.info("app.update_members:id={},appCode={},before={},after={}",appInfo.getId(),appInfo.getAppCode(),appInfo.getMembers(),dto.buildMembers());
        appInfo.setMembers(dto.buildMembers());

        appInfoService.updateById(appInfo);
        appInfoMembersService.saveMembersByStr(appInfo,appInfo.getAppCode());
    }
    public void syncInterfaceMembers(JApiProjectInfo projectInfo,InterfaceManage manage){
        List<MemberRelation> existMembers = memberRelationService.listByInterfaceId(manage.getId());
        List<MemberRelation> newMembers = new ArrayList<>();
        for (JApiProjectOwner partner : projectInfo.getPartners()) {
            MemberRelation relation = partner.toRelation();
            if(relation == null){
                continue;
            }
            relation.setResourceType(ResourceTypeEnum.INTERFACE.getCode());
            relation.setResourceId(manage.getId());
            newMembers.add(relation);
        }
        mergeMembers(existMembers,newMembers);
    }
    public MemberRelation findByUserCode(List<MemberRelation> relations,String userCode){
        for (MemberRelation relation : relations) {
            if(relation.getUserCode().equals(userCode)){
                return relation;
            }
        }
        return null;
    }
    public  void mergeMembers(List<MemberRelation> existMembers,List<MemberRelation> newMembers){
        List<MemberRelation> removed = new ArrayList<>();
        List<MemberRelation> added = new ArrayList<>();
        List<MemberRelation> modified = new ArrayList<>();
        for (MemberRelation existMember : existMembers) {
            boolean isExist = false;
            final MemberRelation found = findByUserCode(newMembers, existMember.getUserCode());
            if(found == null){
               // removed.add(existMember);
            }else{
                if(!found.getResourceRole().equals(existMember.getResourceRole())){
                    found.setId(existMember.getId());
                    modified.add(found);
                }

            }
        }
        for (MemberRelation newMember : newMembers) {
             MemberRelation found = findByUserCode(existMembers, newMember.getUserCode());
            if(found == null){
                added.add(newMember);
            }
        }
        if(!modified.isEmpty()){
            memberRelationService.updateBatchById(modified);
        }
        if(!added.isEmpty()){
            memberRelationService.saveBatch(added);
        }
        if(!removed.isEmpty()){
            memberRelationService.removeByIds(removed.stream().map(MemberRelation::getId).collect(Collectors.toList()));
        }
    }
    public void syncInterfaceMethods(JApiProjectInfo projectInfo,InterfaceManage manage,boolean forceUpdate){
        DataSyncRecord dataSyncRecord = new DataSyncRecord();

        dataSyncRecord.setSource("japi");
        dataSyncRecord.setSourceAppCode(manage.getRelatedId()+"");
        dataSyncRecord.setSourceEnv("apiData");
        try{

            JApiGroupSortTree sortTree = projectInfo.getApiGroupSortTree();

            //  dataSyncRecord.setTargetAppCode(appInfo.getAppCode());
            List<MethodManage> methods = new ArrayList<>();
            methods = methodManageService.getInterfaceMethods(manage.getId());
            Map<Long,MethodManage> japiId2newMethod = new HashMap<>();
            Map<Long,Long> japiId2newId = new HashMap<>();
            //Map<String,List<MethodManage>> path2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getPath));
            Map<Long,List<MethodManage>> relatedId2Methods = groupMethodsByRelatedId(methods);


            dataSyncRecord.setTargetInterfaceId(manage.getId()+"");

            List<MethodManage> added = new ArrayList<>();
            List<MethodManage> updated = new ArrayList<>();
            List<Long> updatedIds = new ArrayList<>();
            List<MethodManage> removed = new ArrayList<>();
            for (JApiGroupSortTree.ApiInfo apiInfo : sortTree.getApiArrForSort()) {
                MethodManage oldMethod = syncJapiApi(apiInfo.getApiID() + "",projectInfo.getProjectID(), manage, relatedId2Methods, added, updated,forceUpdate);
                if(oldMethod != null){
                    if(oldMethod.getId() != null){
                        updatedIds.add(oldMethod.getId());
                    }
                    japiId2newMethod.put(apiInfo.getApiID(),oldMethod);
                }

            }
            for (MethodManage method : methods) {
                if(!updatedIds.contains(method.getId())
                        && method.getRelatedId() != null
                ){ // 在线联调创建的方法需要移除
                    removed.add(method);
                }
            }
            if(!added.isEmpty()){
                /*for (MethodManage methodManage : added) {
                    try{
                        methodManageService.save(methodManage);
                    }catch (Exception e){
                        log.error("api.err_save_api,MethodManage={}",JsonUtils.toJSONString(methodManage),e);
                    }
                }*/
                methodManageService.saveBatch(added);
            }
            if(!removed.isEmpty()){
                List<Long> methodIds = removed.stream().map(item -> item.getId()).collect(Collectors.toList());
                methodManageService.removeByIds(methodIds);
            }
            for (MethodManage methodManage : updated) {
                methodManageService.updateById(methodManage);
            }
            for (Map.Entry<Long, MethodManage> entry : japiId2newMethod.entrySet()) {
                japiId2newId.put(entry.getKey(),entry.getValue().getId());
            }
            String groupLastVersion = manage.getGroupLastVersion();
            if(StringHelper.isBlank(groupLastVersion)){
                groupLastVersion = "";
            }
            String japiVersion = sortTree.getSortTreeVersion()+"";
            int timeLength = DateUtil.getCurrentDateMillTime().length();
            if(forceUpdate ||   groupLastVersion.length() < timeLength ){ // japi的分组版本长度小于在线联调的分组版本长度，因此这里采用单向数据同步
                Map<Long, Long> japiGroupId2GroupId = mergeApiGroup(sortTree, manage.getId());

                manage.setGroupLastVersion(japiVersion);// japi保存的是秒时间，我们这边保存的是毫秒
                manage.setSortGroupTree(sortTree.toGroupTreeModel(japiGroupId2GroupId,japiId2newId ));
                interfaceManageService.updateById(manage);
            }
            dataSyncRecord.setSuccess(1);
        }catch (Exception e){
            dataSyncRecord.setSuccess(0);
            dataSyncRecord.setErrorInfo(SwaggerParserService.truncateStr(e.getMessage(),500));
            log.error("japi.err_sync_api_data:projectId={},projectName={}",projectInfo.getProjectID(),projectInfo.getProjectName(),e);
        }
        dataSyncRecordService.save(dataSyncRecord);
    }
    public void syncInterfaceDesc(InterfaceManage interfaceManage,Long projectId){
        final RequestClient client = getClient(null);
        Map<String,Object> params = new HashMap<>();
        params.put("projectID",projectId);
        String result = client.post("/external/getProjectDesDocNoAuth", params, null);
        final Map map = JsonUtils.parse(result, Map.class);
        String desc = (String) map.get("projectDesDoc");
        if(!StringHelper.isBlank(desc)){
            if(SwaggerParserService.getActualLength(desc) <= 65535){
                interfaceManage.setDocInfo(desc);
            }

        }
    }
    private JapiData getJApiApiData(String apiId, Long projectID){
        RequestClient client = new RequestClient(JAPI_BASE_URL,null);
        Map<String,Object> params = new HashMap<>();
        params.put("apiID",apiId);
        params.put("projectID",projectID);
        String result = client.get("/external/getApiNoAuth", params);
        Map map = JsonUtils.parse(result, Map.class);
        Map apiDataMap = (Map) map.get("data");
        JapiData apiData = JsonUtils.cast(apiDataMap, JapiData.class);
        return apiData;
    }
    private String getJapiMockTemplate(MethodManage methodManage,Long projectID){
        String mockPath = "/mocker/data";//?p=1838&v=POST&u=/userInfo/addUser
        RequestClient client = new RequestClient(JAPI_BASE_URL,null);
        Map<String,Object> params = new HashMap<>();
        params.put("p",projectID);
        params.put("v", methodManage.getHttpMethod());
        params.put("u",methodManage.getPath());
        String result = client.get(mockPath, params);
        return result;
    }
    public void updateMockTemplateUseJapiData(InterfaceManage interfaceManage,MethodManage methodManage,Long projectId){
        try{
            if(methodManage.getRelatedId() == null) return;
            String result = getJapiMockTemplate(methodManage, projectId);
            log.info("japi.mock_template,methodId={},result={}",methodManage.getId(),result);

            Integer relatedId = testEasyMockRemoteService.addHttpMethod(interfaceManage, methodManage);

            if(relatedId == null){
                log.info("japi.miss_related_id:methodId={},realtedId={}",methodManage.getId(),relatedId);
                return;
            }
            testEasyMockRemoteService.updateHttpDefaultTemplate(methodManage,relatedId+"",result);
        }catch (Exception e){
            log.error("japi.err_update_mock_template,methodId={}",methodManage.getId(),e);
        }

    }
    public void updateInterfaceJapiMockInfo(Long interfaceId){
        Guard.notEmpty(interfaceId,"interfaceId不可为空");
        InterfaceManage interfaceMannge = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceMannge,"接口不存在");
        updateInterfaceJapiMockInfo(interfaceMannge);
    }
    public void updateInterfaceJapiMockInfo(InterfaceManage interfaceManage){
        List<MethodManage> interfaceMethods = methodManageService.getInterfaceMethods(interfaceManage.getId());
        MetaContextHelper.skipModify(true);
        UserSessionLocal.setUser(UserSessionLocal.getUser());
        for (MethodManage interfaceMethod : interfaceMethods) {
            try{
                if(interfaceMethod.getRelatedId() == null) continue;
                MethodManage fullMethodInfo = methodManageService.getById(interfaceMethod.getId());
                methodManageService.initContentObject(fullMethodInfo);
                updateMockTemplateUseJapiData(interfaceManage,fullMethodInfo,interfaceManage.getRelatedId());
                JapiData japiData = getJApiApiData(fullMethodInfo.getRelatedId() + "", interfaceManage.getRelatedId());
                MethodManage newMethod = japiData.toMethod();
                if(!hasMockExpr((HttpMethodModel) newMethod.getContentObject())){
                    continue;
                }
                if(isAfter(fullMethodInfo.getModified().getTime() , newMethod.getModified().getTime() )){
                    continue;
                }
                LambdaUpdateWrapper<MethodManage> luw = new LambdaUpdateWrapper<>();
                luw.eq(MethodManage::getId,interfaceMethod.getId());
                luw.set(MethodManage::getContent,newMethod.getContent());
                //luw.set(MethodManage::getModified,newMethod.getModified());
                methodManageService.update(luw);
            }catch (Exception e){
                log.error("japi.err_update_http_method:methodId={}",interfaceMethod.getId(),e);
            }

        }
        MetaContextHelper.clearModifyState();
    }
    private  boolean hasMockExpr(JsonType jsonType){
        boolean result = jsonType != null &&
                (StringUtils.isNotBlank(jsonType.getMock()) || jsonType.getConstraint() != null)
                ;
        if(result) return result;
        if(jsonType instanceof ComplexJsonType){
            if(ObjectHelper.isEmpty(((ComplexJsonType) jsonType).getChildren())) return false;
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                if(hasMockExpr(child)) return true;
            }
        }
        return false;
    }
    private  boolean hasMockExpr(List<? extends JsonType> jsonTypes){
        if(CollectionUtils.isEmpty(jsonTypes)) return false;
        for(JsonType jsonType:jsonTypes){
            if(hasMockExpr(jsonType)) return true;
        }
        return false;
    }
    private  boolean hasMockExpr(HttpMethodModel methodModel){
        return hasMockExpr(methodModel.getInput().getHeaders()) ||
                hasMockExpr(methodModel.getInput().getParams()) ||
                hasMockExpr(methodModel.getInput().getPath()) ||
                hasMockExpr(methodModel.getInput().getBody()) ||
                hasMockExpr(methodModel.getOutput().getHeaders()) ||
                hasMockExpr(methodModel.getOutput().getBody());
    }

    private void initRelatedField(String apiId,Long projectID,Map<String,List<MethodManage>> path2Methods){
        JapiData apiData = getJApiApiData(apiId, projectID);
        if(apiData == null) return ;
        MethodManage newMethod = apiData.toMethod();
        List<MethodManage> oldMethods = path2Methods.get(apiData.getBaseInfo().getApiURI());
        MethodManage exist = null;
        if(oldMethods == null){

        }else if(oldMethods.size() == 1){
            exist = oldMethods.get(0);
        }else if(oldMethods.size() >1 ){
            for (MethodManage oldMethod : oldMethods) {
                if(oldMethod.getHttpMethod().equalsIgnoreCase(newMethod.getHttpMethod() )){
                    exist = oldMethod;
                }
            }
        }
        exist.setRelatedId(apiData.getBaseInfo().getApiID());
        methodManageService.updateRelatedId(exist.getInterfaceId(),exist.getRelatedId());
    }
    public MethodManage syncJapiApi(String apiId,Long projectID, InterfaceManage interfaceManage,Map<Long,List<MethodManage>> id2Methods,
                            List<MethodManage> added,List<MethodManage> updated,boolean forceUpdate
                            ){

        JapiData apiData = getJApiApiData(apiId,projectID);
        if(apiData == null) return null;
        MethodManage newMethod = apiData.toMethod();
        MethodDocConfig docConfig = new MethodDocConfig();
        docConfig.setDocType("md");

        newMethod.setDocConfig(docConfig);
        newMethod.setYn(DataYnEnum.VALID.getCode());
        newMethod.setInterfaceId(interfaceManage.getId());
        List<MethodManage> oldMethods = id2Methods.get(apiData.getBaseInfo().getApiID());
        MethodManage exist = null;
        if(oldMethods == null){
            added.add(newMethod);
            return newMethod;
        }else if(oldMethods.size() == 1){
            exist = oldMethods.get(0);
        }/*else if(oldMethods.size() >1 ){
            for (MethodManage oldMethod : oldMethods) {
                if(oldMethod.getHttpMethod().equalsIgnoreCase(newMethod.getHttpMethod() )){
                    exist = oldMethod;
                }
            }
        }*/
        if(exist != null){
            if(
                    isAfter(exist.getModified().getTime() , newMethod.getModified().getTime() ) && !forceUpdate
            ){ // 当前方法在在线联调修改了，在j-api没有修改，说明不需要同步呢，否则认为需要同步
                return exist;
            }else{
                updated.add(exist);
                Long id  = exist.getId();
                BeanUtils.copyProperties(newMethod,exist);
                exist.setId(id);

            }
        }
        return exist;
    }


    /**
     * 过滤评论中所包含的表情符号
     * @param str
     * @return
     */
   /* public static String emojiFilter(String str) {
        try {
            Pattern pattern = Pattern.compile(unicodeReg);
            StringBuilder reBuffer = new StringBuilder();
            for (int i = 0; i < str.length(); i++) {
                char c = str.charAt(i);
                String temp = String.valueOf(c);
                Matcher matcher = pattern.matcher(temp);
                if (matcher.find()) {
                    reBuffer.append(temp);
                }
            }
            return reBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return str; // 转换出错就放弃转换
        }
    }*/
    /**
     * 过滤评论中所包含的表情符号
     * @param content
     * @return
     */
    public static String emojiFilter(String content) {
        if(StringHelper.isEmpty(content)) return content;
        StringBuilder sb = new StringBuilder();
        for (char ch : content.toCharArray()) {
            if (!Character.isHighSurrogate(ch) && !Character.isLowSurrogate(ch)) {
                sb.append(ch);
            }
        }
        /*if (sb.length() < content.length()) {
            log.info(content.length() + " 过滤掉 " + sb.length());
        }*/
        return sb.toString();
    }



    public static void main(String[] args) {
        String data = " 版权声明：本文为CSDN博主「jack.lei11231」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。\uD83D\uDC2D\uD83D\uDC02\uD83D\uDC2F\uD83D\uDC30\uD83D\uDC32\uD83D\uDC0D\uD83D\uDC0E\uD83D\uDC11\uD83D\uDC12\uD83D\uDC14\uD83D\uDC36\uD83D\uDC37";
       // System.out.println(filterSupplement(data));
        System.out.println(emojiFilter(data));
        System.out.println(data);
    }
}
