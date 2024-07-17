# 检查是否为生产环境
if [[ -n "${JDOS_ENV}" && "${JDOS_ENV}" == "pro" ]]; then
    echo "当前环境为生产环境，不生效"
    exit 0
fi

_current_dir=$(pwd)

echo "current_dir_is:${_current_dir}"
# 1 获取Java版本号

if [[ -n "${JAVA_HOME}" ]]; then
  echo "java home exist"
  echo "java__HOME ${JAVA_HOME}"
   if [[ $JAVA_HOME ==  *"local"*  ]];then
      echo "科技站jdos环境"
	 JAVA_VERSION=$(echo ${JAVA_HOME}>&1 | awk -F 'local/' '{print $2}')
   else
    echo "零售jdos环境"
    JAVA_VERSION=$(echo ${JAVA_HOME}>&1 | awk -F 'servers/' '{print $2}')
   fi

   export JAVA_VERSION
   echo "java__Version ${JAVA_VERSION}"
elif [which java >/dev/null]; then
   echo "java cmd exist"
   JAVA_VERSION_NO=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F'.' '{print $1"."$2"."$3}' | awk -F'_' '{print $1"_"$2}')
   JAVA_VERSION=$(ls /export/servers | grep "${JAVA_VERSION}")
   export JAVA_VERSION
elif [ -f /export/Domains/${DOMAIN}/server1/bin/start.sh ]; then
     echo "java default exist"
    JAVA_VERSION=$(cat /export/Domains/${DOMAIN}/server1/bin/start.sh 2>&1 | awk -F'servers/' '/JAVA_HOME=/ {print $2}')
    export JAVA_VERSION
else
   echo "未找到jdk安装路径，如果是java命令启动方式请确保JAVA_HOME或者java命令存在"
   exit 0
fi


echo "java-version::: $JAVA_VERSION"

if [[ -n "$JAVA_VERSION" ]];then

  if [ -d "/export/servers/${JAVA_VERSION}/jre/lib/amd64/server" ];then
    	export JDK_SO_PATH="/export/servers/$JAVA_VERSION/jre/lib/amd64"
  elif [ -d "/export/local/${JAVA_VERSION}/jre/lib/amd64/server" ]; then
     export JDK_SO_PATH="/export/local/$JAVA_VERSION/jre/lib/amd64"
  else
    echo "未找到jdk安装路径，如果是java命令启动方式请确保JAVA_HOME或者java命令存在"
    exit 0
  fi

	echo "jdk so path is ::$JDK_SO_PATH"
	# 获取子版本号
	version=$(echo $JAVA_VERSION | awk -F'[_]' '{print $NF}')

	#2 进入指定目录
	cd $JDK_SO_PATH/server
	mv libjvm.so libjvm.so.bak


	#3 下载so文件，注意so文件需要和当前版本相匹配； 注意补齐匹配规则；
  if [ "$version" == "60" ] || [ "$version" == "61" ]
  then
    wget -O libjvm.so  http://storage.jd.local/solution/hot/jvm/libjvm_60.so
  elif [ "$version" == "90" ] || [ "$version" == "91" ]
  then
    wget -O libjvm.so  http://storage.jd.local/solution/hot/jvm/libjvm_92.so
  else
    wget http://storage.jd.local/solution/hot/libjvm.so
  fi

#	#3 下载so文件，注意so文件需要和当前192版本相匹配
#	wget http://storage.jd.local/solution/hot/libjvm.so

	#4 copy到指定目录
	mkdir $JDK_SO_PATH/dcevm
	cp libjvm.so $JDK_SO_PATH/dcevm
	cd /export/data

  #add移除JAVA_OPTS，用户设置的debug
  export JAVA_OPTS=$(echo $JAVA_OPTS | sed -r 's/-Xrunjdwp:transport=[^ ]*//g')
  export CATALINA_OPTS=$(echo $CATALINA_OPTS | sed -r 's/-Xrunjdwp:transport=[^ ]*//g')

	#wget -N http://storage.jd.local/solution/hot/fast-deploy-1.1-SNAPSHOT.jar
	wget  -O fast-deploy-1.1-SNAPSHOT.jar "http://data-flow.jd.com/plugin/getAgent?appCode=$APP_NAME&ip=$POD_IP"
  export CATALINA_OPTS="${CATALINA_OPTS} -javaagent:/export/data/fast-deploy-1.1-SNAPSHOT.jar   -DLOGFILE=/export/Logs/hotupdate.log   "
	export HOTUPDATE_NO_DEBUG_OPTS="$CATALINA_OPTS"
	export CATALINA_OPTS="$CATALINA_OPTS -agentlib:jdwp=transport=dt_socket,address=5005,suspend=n,server=y,quiet=n"
	export HOTUPDATE_OPTS="$CATALINA_OPTS"
	export JAVA_OPTS=$(echo $JAVA_OPTS | sed -r 's/-XX:\+Use(ConcMarkSweepGC|ParallelGC|G1GC|ParNewGC)//g')

	echo "HOTUPDATE_NO_DEBUG_OPTS is $HOTUPDATE_NO_DEBUG_OPTS"
	echo "HOTUPDATE_OPTS is $HOTUPDATE_OPTS"
	echo "CATALINA_OPTS is $CATALINA_OPTS"
else
   echo "未找到jdk安装路径，如果是java命令启动方式请确保JAVA_HOME或者java命令存在"
fi


cd "$_current_dir"

echo "recovery_current_dir:${_current_dir}"
