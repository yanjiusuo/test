package com.jd.workflow.jsf.analyzer;

import com.google.common.io.Files;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.DependencyCollectionException;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;
import org.eclipse.aether.util.repository.SimpleArtifactDescriptorPolicy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AetherJarDownloader {

    /**
     *  create a repository system session
     * @param system RepositorySystem
     * @return RepositorySystemSession
     */
    private static RepositorySystemSession newSession( RepositorySystem system,String target )
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
        session.setArtifactDescriptorPolicy( new SimpleArtifactDescriptorPolicy(true, true) );
        LocalRepository localRepo = new LocalRepository( /*"target/local-repo" */target);
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        return session;
    }
    /**
     * 建立RepositorySystem
     * @return RepositorySystem
     */
    private static RepositorySystem newRepositorySystem()
    {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService( RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class );
        locator.addService( TransporterFactory.class, FileTransporterFactory.class );
        locator.addService( TransporterFactory.class, HttpTransporterFactory.class );

        return locator.getService( RepositorySystem.class );
    }
    public static List<File> downloadAllJar(MavenJarLocation location, File targetFile) throws  DependencyResolutionException, DependencyCollectionException {
        String groupId=location.getGroupId();
        String artifactId=location.getArtifactId();
        String version=location.getVersion();

        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession( repoSystem ,targetFile.getAbsolutePath());

        /**
         * 下载一个jar包
         */
        Artifact artifact=new DefaultArtifact(groupId+":"+artifactId+":"+version);

        Dependency dependency = new Dependency(artifact, null);
        CollectRequest collectRequest = new CollectRequest();
        collectRequest.setRoot(dependency);
        for (RemoteRepository remoteRepository : getJdRepository()) {
            collectRequest.addRepository(remoteRepository);
        }

        DependencyNode node = repoSystem.collectDependencies(session, collectRequest).getRoot();

        DependencyRequest dependencyRequest = new DependencyRequest();
        dependencyRequest.setRoot(node);
        dependencyRequest.setFilter(new DependencyFilter() {
            @Override
            public boolean accept(DependencyNode node, List<DependencyNode> parents) {
                log.info("开始下载依赖：{}",node.getArtifact().toString());
                return true;
            }
        });

        repoSystem.resolveDependencies(session, dependencyRequest);

        PreorderNodeListGenerator nlg = new PreorderNodeListGenerator();

        node.accept(nlg);

        //此时就已经下载好了 打印出jars
        //System.out.println(nlg.getFiles());

        return nlg.getFiles();

    }

    private static List<RemoteRepository> getJdRepository(){
        List<RemoteRepository>  list = new ArrayList<>();
        list.add((new RemoteRepository.Builder( "artifactory", "default", "https://artifactory.jd.com/libs-snapshots")
                .setSnapshotPolicy(new RepositoryPolicy(true,RepositoryPolicy.UPDATE_POLICY_ALWAYS,RepositoryPolicy.CHECKSUM_POLICY_WARN)).build()));
        list.add(new RemoteRepository.Builder( "snapshots", "default", "https://artifactory.jd.com/libs-releases" ).build());
        list.add(new RemoteRepository.Builder( "central", "default", "https://maven.aliyun.com/repository/central" ).build());

        {

            list.add(new RemoteRepository.Builder( "daojia", "default", "http://nexus.corp.imdada.cn/content/repositories/daojiaRepo/" ).build());
            list.add(new RemoteRepository.Builder( "dadaarchetype", "default", "http://nexus.corp.imdada.cn/content/repositories/public/" ).build());
            list.add(new RemoteRepository.Builder( "daddacentral", "default", "http://nexus.corp.imdada.cn/content/repositories/public/" ).build());
        }
        return list;
    }
    public static String getJarLatestVersion(MavenJarLocation location){
        File tempDir = Files.createTempDir();
        try{
            return getJarLatestVersion(location,tempDir);
        }finally {
            FileUtils.deleteQuietly(tempDir);

        }
    }
    public static String getJarLatestVersion(MavenJarLocation location, File targetFile){
        // 需要检查的jar包坐标
        String groupId=location.getGroupId();
        String artifactId=location.getArtifactId();
        String version=location.getVersion();



        // 创建Aether库相关的对象
        RepositorySystem repositorySystem = newRepositorySystem();
        RepositorySystemSession session =  newSession( repositorySystem ,targetFile.getAbsolutePath());;

        Artifact artifact=new DefaultArtifact(groupId+":"+artifactId+":"+version);
        ArtifactRequest artifactRequest=new ArtifactRequest();

        for (RemoteRepository remoteRepository : getJdRepository()) {
            artifactRequest.addRepository(remoteRepository);
        }


        artifactRequest.setArtifact(artifact);

        try {
            VersionRequest request = new VersionRequest();
            request.setArtifact(artifact);
            for (RemoteRepository remoteRepository : getJdRepository()) {
                request.addRepository(remoteRepository);
            }

            VersionResult result = repositorySystem.resolveVersion(session, request);
            return result.getVersion();
        }catch (Exception e){
            throw new BizException("获取jar包最新更新时间失败："+e.getMessage(),e);
        }
    }
    public static boolean existMockJar(MavenJarLocation location,File targetFile){
        // 需要检查的jar包坐标
        String groupId=location.getGroupId();
        String artifactId=location.getArtifactId();
        String version=location.getVersion();

        // 仓库信息
        String repositoryUrl = "https://repo.maven.apache.org/maven2";
        String repositoryId = "central";

        // 创建Aether库相关的对象
        RepositorySystem repositorySystem = newRepositorySystem();
        RepositorySystemSession session =  newSession( repositorySystem ,targetFile.getAbsolutePath());;

        Artifact artifact=new DefaultArtifact(groupId+":"+artifactId+":"+version);
        ArtifactRequest artifactRequest=new ArtifactRequest();

        for (RemoteRepository remoteRepository : getJdRepository()) {
            artifactRequest.addRepository(remoteRepository);
        }
        artifactRequest.setArtifact(artifact);

        try {

            final ArtifactResult result = repositorySystem.resolveArtifact(session, artifactRequest);


            // 获取jar包文件路径
            File jarFile = result.getArtifact().getFile();
            if (jarFile.exists()) {
                log.info("artifact.success_resolve_maven_location:groupId={},artifactId={},version={}",groupId,artifactId,version);
                return true;
            } else {
                log.info("artifact.err_not_exist_maven_location:groupId={},artifactId={},version={}",groupId,artifactId,version);
                return false;
            }
        } catch (ArtifactResolutionException e) {
            log.error("artifact.err_resolve_maven_location:groupId={},artifactId={},version={}",groupId,artifactId,version,e);
            return false;
        }

    }
    public static File downloadSingleJar(MavenJarLocation location, File targetFile) throws ArtifactResolutionException
    {
        String groupId=location.getGroupId();
        String artifactId=location.getArtifactId();
        String version=location.getVersion();

        RepositorySystem repoSystem = newRepositorySystem();
        RepositorySystemSession session = newSession( repoSystem ,targetFile.getAbsolutePath());
       /* RemoteRepository central=null;
        if(username==null&&password==null)
        {
            central = new RemoteRepository.Builder( "central", "default", "https://artifactory.jd.com/libs-releases" ).build();
        }else{
            Authentication authentication=new AuthenticationBuilder().addUsername(username).addPassword(password).build();
            central = new RemoteRepository.Builder( "central", "default", repositoryUrl ).setAuthentication(authentication).build();
        }*/
        /**
         * 下载一个jar包
         */
        Artifact artifact=new DefaultArtifact(groupId+":"+artifactId+":"+version);
        ArtifactRequest artifactRequest=new ArtifactRequest();
       // artifactRequest.addRepository(new RemoteRepository.Builder( "central", "default", "https://artifactory.jd.com/libs-releases" ).build());
       /* if(StringUtils.containsIgnoreCase(version,"SNAPSHOT")){
            artifactRequest.addRepository(new RemoteRepository.Builder( "central", "default", "https://artifactory.jd.com/libs-snapshots" ).build());
        }else{

            artifactRequest.addRepository(new RemoteRepository.Builder( "central", "default", "https://artifactory.jd.com/libs-releases" ).build());
        }*/
        for (RemoteRepository remoteRepository : getJdRepository()) {
            artifactRequest.addRepository(remoteRepository);
        }

        artifactRequest.setArtifact(artifact);
        final ArtifactResult artifactResult = repoSystem.resolveArtifact(session, artifactRequest);

        return artifactResult.getArtifact().getFile();

    }


    public static void main(String[] args) throws ArtifactResolutionException, DependencyResolutionException, DependencyCollectionException {
        MavenJarLocation params = new MavenJarLocation();
        params.setGroupId("com.dada.crowdsourcing.works");
        params.setArtifactId("crowdsourcing-works-jsf-api");
        params.setVersion("1.0.0-SNAPSHOT");
         downloadAllJar(params, new File("d:/target/local-repo") );

     /*   MavenJarLocation params = new MavenJarLocation();
        params.setGroupId("com.jd.wjf.test");
        params.setArtifactId("jar-demo");
        params.setVersion("1.0-SNAPSHOT");
        downloadAllJar(params, new File("d:/target/local-repo") );*/
       /* try{
            MavenJarLocation params = new MavenJarLocation();
            params.setGroupId("org.apache.logging.log4j");
            params.setArtifactId("log4j-core");
            params.setVersion("2.3");
            downloadAllJar(params, new File("d:/target/local-repo") );
        }catch (Exception e){
            e.printStackTrace();
        }*/  /*try{
            MavenJarLocation params = new MavenJarLocation();
            params.setGroupId("com.jd.wjf.test");
            params.setArtifactId("jar-demo");
            params.setVersion("1.0-SNAPSHOT");
            downloadAllJar(params, new File("d:/target/local-repo") );
        }catch (Exception e){
            e.printStackTrace();
        }*/


    }

}
