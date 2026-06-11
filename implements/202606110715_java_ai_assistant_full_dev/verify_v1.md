# 验证报告（v1）

## 结果
PASSED

## 统计
- 通过：17
- 失败：0
- 跳过：0

## 说明
- 首次执行 `mvn -f java-ai-assistant/pom.xml verify` 失败，原因为环境缺少 `mvn`。
- 已安装 OpenJDK 17 与 Maven 后重试同一验证命令，Maven verify 成功，退出码为 0。

## 测试执行日志

命令：`mvn -f java-ai-assistant/pom.xml verify`

```text
/bin/bash: line 1: mvn: command not found

[MAVEN_EXIT_CODE=127]
```

## 环境准备日志

```text
Hit:1 https://cli.github.com/packages stable InRelease
Hit:2 https://deb.nodesource.com/node_20.x nodistro InRelease
Hit:3 https://repos-droplet.digitalocean.com/apt/droplet-agent main InRelease
Hit:4 http://mirrors.digitalocean.com/ubuntu noble InRelease
Get:5 http://security.ubuntu.com/ubuntu noble-security InRelease [126 kB]
Hit:6 http://mirrors.digitalocean.com/ubuntu noble-updates InRelease
Hit:7 http://mirrors.digitalocean.com/ubuntu noble-backports InRelease
Get:8 http://security.ubuntu.com/ubuntu noble-security/main amd64 Packages [1764 kB]
Get:9 http://security.ubuntu.com/ubuntu noble-security/universe amd64 Packages [1195 kB]
Fetched 3085 kB in 12s (262 kB/s)
Reading package lists...
Reading package lists...
Building dependency tree...
Reading state information...
The following packages were automatically installed and are no longer required:
  libfwupd2 libgusb2
Use 'apt autoremove' to remove them.
The following additional packages will be installed:
  adwaita-icon-theme alsa-topology-conf alsa-ucm-conf at-spi2-common
  at-spi2-core ca-certificates-java dconf-gsettings-backend dconf-service
  fontconfig fonts-dejavu-extra gsettings-desktop-schemas
  gtk-update-icon-cache hicolor-icon-theme humanity-icon-theme java-common
  libaopalliance-java libapache-pom-java libasound2-data libasound2t64
  libatinject-jsr330-api-java libatk-bridge2.0-0t64 libatk-wrapper-java
  libatk-wrapper-java-jni libatk1.0-0t64 libatspi2.0-0t64 libavahi-client3
  libavahi-common-data libavahi-common3 libcairo-gobject2 libcairo2
  libcdi-api-java libcommons-cli-java libcommons-io-java libcommons-lang3-java
  libcommons-parent-java libcups2t64 libdatrie1 libdconf1 libdrm-intel1
  liberror-prone-java libgail-common libgail18t64 libgbm1 libgdk-pixbuf-2.0-0
  libgdk-pixbuf2.0-bin libgdk-pixbuf2.0-common
  libgeronimo-annotation-1.3-spec-java libgeronimo-interceptor-3.0-spec-java
  libgif7 libgl1 libgl1-mesa-dri libglvnd0 libglx-mesa0 libglx0 libgraphite2-3
  libgtk2.0-0t64 libgtk2.0-bin libgtk2.0-common libguava-java libguice-java
  libharfbuzz0b libice-dev libice6 libjansi-java libjsr305-java liblcms2-2
  libllvm20 libmaven-parent-java libmaven-resolver-java
  libmaven-shared-utils-java libmaven3-core-java libpango-1.0-0
  libpangocairo-1.0-0 libpangoft2-1.0-0 libpciaccess0 libpcsclite1
  libpixman-1-0 libplexus-cipher-java libplexus-classworlds-java
  libplexus-component-annotations-java libplexus-interpolation-java
  libplexus-sec-dispatcher-java libplexus-utils2-java libpthread-stubs0-dev
  librsvg2-2 librsvg2-common libsisu-inject-java libsisu-plexus-java
  libslf4j-java libsm-dev libsm6 libthai-data libthai0 libvulkan1
  libwagon-file-java libwagon-http-shaded-java libwagon-provider-api-java
  libwayland-client0 libx11-dev libx11-xcb1 libxau-dev libxaw7 libxcb-dri3-0
  libxcb-glx0 libxcb-present0 libxcb-randr0 libxcb-render0 libxcb-shape0
  libxcb-shm0 libxcb-sync1 libxcb-xfixes0 libxcb1-dev libxcomposite1
  libxcursor1 libxdamage1 libxdmcp-dev libxfixes3 libxft2 libxi6 libxinerama1
  libxkbfile1 libxmu6 libxrandr2 libxrender1 libxshmfence1 libxt-dev libxt6t64
  libxtst6 libxv1 libxxf86dga1 libxxf86vm1 mesa-libgallium mesa-vulkan-drivers
  openjdk-17-jdk-headless openjdk-17-jre openjdk-17-jre-headless
  session-migration ubuntu-mono x11-common x11-utils x11proto-dev
  xorg-sgml-doctools xtrans-dev
Suggested packages:
  default-jre alsa-utils libasound2-plugins libatinject-jsr330-api-java-doc
  libel-api-java libcommons-io-java-doc cups-common gvfs libasm-java
  libcglib-java libice-doc libjsr305-java-doc liblcms2-utils
  libmaven-shared-utils-java-doc liblogback-java pcscd
  libplexus-utils2-java-doc librsvg2-bin junit4 testng libcommons-logging-java
  liblog4j1.2-java libsm-doc libx11-doc libxcb-doc libxt-doc openjdk-17-demo
  openjdk-17-source visualvm libnss-mdns fonts-ipafont-gothic
  fonts-ipafont-mincho fonts-wqy-microhei | fonts-wqy-zenhei fonts-indic
  mesa-utils
Recommended packages:
  luit
The following NEW packages will be installed:
  adwaita-icon-theme alsa-topology-conf alsa-ucm-conf at-spi2-common
  at-spi2-core ca-certificates-java dconf-gsettings-backend dconf-service
  fontconfig fonts-dejavu-extra gsettings-desktop-schemas
  gtk-update-icon-cache hicolor-icon-theme humanity-icon-theme java-common
  libaopalliance-java libapache-pom-java libasound2-data libasound2t64
  libatinject-jsr330-api-java libatk-bridge2.0-0t64 libatk-wrapper-java
  libatk-wrapper-java-jni libatk1.0-0t64 libatspi2.0-0t64 libavahi-client3
  libavahi-common-data libavahi-common3 libcairo-gobject2 libcairo2
  libcdi-api-java libcommons-cli-java libcommons-io-java libcommons-lang3-java
  libcommons-parent-java libcups2t64 libdatrie1 libdconf1 libdrm-intel1
  liberror-prone-java libgail-common libgail18t64 libgbm1 libgdk-pixbuf-2.0-0
  libgdk-pixbuf2.0-bin libgdk-pixbuf2.0-common
  libgeronimo-annotation-1.3-spec-java libgeronimo-interceptor-3.0-spec-java
  libgif7 libgl1 libgl1-mesa-dri libglvnd0 libglx-mesa0 libglx0 libgraphite2-3
  libgtk2.0-0t64 libgtk2.0-bin libgtk2.0-common libguava-java libguice-java
  libharfbuzz0b libice-dev libice6 libjansi-java libjsr305-java liblcms2-2
  libllvm20 libmaven-parent-java libmaven-resolver-java
  libmaven-shared-utils-java libmaven3-core-java libpango-1.0-0
  libpangocairo-1.0-0 libpangoft2-1.0-0 libpciaccess0 libpcsclite1
  libpixman-1-0 libplexus-cipher-java libplexus-classworlds-java
  libplexus-component-annotations-java libplexus-interpolation-java
  libplexus-sec-dispatcher-java libplexus-utils2-java libpthread-stubs0-dev
  librsvg2-2 librsvg2-common libsisu-inject-java libsisu-plexus-java
  libslf4j-java libsm-dev libsm6 libthai-data libthai0 libvulkan1
  libwagon-file-java libwagon-http-shaded-java libwagon-provider-api-java
  libwayland-client0 libx11-dev libx11-xcb1 libxau-dev libxaw7 libxcb-dri3-0
  libxcb-glx0 libxcb-present0 libxcb-randr0 libxcb-render0 libxcb-shape0
  libxcb-shm0 libxcb-sync1 libxcb-xfixes0 libxcb1-dev libxcomposite1
  libxcursor1 libxdamage1 libxdmcp-dev libxfixes3 libxft2 libxi6 libxinerama1
  libxkbfile1 libxmu6 libxrandr2 libxrender1 libxshmfence1 libxt-dev libxt6t64
  libxtst6 libxv1 libxxf86dga1 libxxf86vm1 maven mesa-libgallium
  mesa-vulkan-drivers openjdk-17-jdk openjdk-17-jdk-headless openjdk-17-jre
  openjdk-17-jre-headless session-migration ubuntu-mono x11-common x11-utils
  x11proto-dev xorg-sgml-doctools xtrans-dev
0 upgraded, 145 newly installed, 0 to remove and 0 not upgraded.
Need to get 208 MB of archives.
After this operation, 662 MB of additional disk space will be used.
Get:1 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgdk-pixbuf2.0-common all 2.42.10+dfsg-3ubuntu3.3 [8302 B]
Get:2 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgdk-pixbuf-2.0-0 amd64 2.42.10+dfsg-3ubuntu3.3 [147 kB]
Get:3 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 gtk-update-icon-cache amd64 3.24.41-4ubuntu1.3 [51.9 kB]
Get:4 http://mirrors.digitalocean.com/ubuntu noble/main amd64 hicolor-icon-theme all 0.17-2 [9976 B]
Get:5 http://mirrors.digitalocean.com/ubuntu noble/main amd64 humanity-icon-theme all 0.6.16 [1282 kB]
Get:6 http://mirrors.digitalocean.com/ubuntu noble/main amd64 ubuntu-mono all 24.04-0ubuntu1 [151 kB]
Get:7 http://mirrors.digitalocean.com/ubuntu noble/main amd64 adwaita-icon-theme all 46.0-1 [723 kB]
Get:8 http://mirrors.digitalocean.com/ubuntu noble/main amd64 alsa-topology-conf all 1.2.5.1-2 [15.5 kB]
Get:9 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libasound2-data all 1.2.11-1ubuntu0.2 [21.3 kB]
Get:10 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libasound2t64 amd64 1.2.11-1ubuntu0.2 [398 kB]
Get:11 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 alsa-ucm-conf all 1.2.10-1ubuntu5.11 [69.3 kB]
Get:12 http://mirrors.digitalocean.com/ubuntu noble/main amd64 at-spi2-common all 2.52.0-1build1 [8674 B]
Get:13 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxi6 amd64 2:1.8.1-1build1 [32.4 kB]
Get:14 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libatspi2.0-0t64 amd64 2.52.0-1build1 [80.5 kB]
Get:15 http://mirrors.digitalocean.com/ubuntu noble/main amd64 x11-common all 1:7.7+23ubuntu3 [21.7 kB]
Get:16 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxtst6 amd64 2:1.2.3-1.1build1 [12.6 kB]
Get:17 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libdconf1 amd64 0.40.0-4ubuntu0.1 [39.6 kB]
Get:18 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 dconf-service amd64 0.40.0-4ubuntu0.1 [27.6 kB]
Get:19 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 dconf-gsettings-backend amd64 0.40.0-4ubuntu0.1 [22.1 kB]
Get:20 http://mirrors.digitalocean.com/ubuntu noble/main amd64 session-migration amd64 0.3.9build1 [9034 B]
Get:21 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 gsettings-desktop-schemas all 46.1-0ubuntu1 [35.6 kB]
Get:22 http://mirrors.digitalocean.com/ubuntu noble/main amd64 at-spi2-core amd64 2.52.0-1build1 [56.6 kB]
Get:23 http://mirrors.digitalocean.com/ubuntu noble/main amd64 ca-certificates-java all 20240118 [11.6 kB]
Get:24 http://mirrors.digitalocean.com/ubuntu noble/main amd64 fontconfig amd64 2.15.0-1.1ubuntu2 [180 kB]
Get:25 http://mirrors.digitalocean.com/ubuntu noble/main amd64 fonts-dejavu-extra all 2.37-8 [1947 kB]
Get:26 http://mirrors.digitalocean.com/ubuntu noble/main amd64 java-common all 0.75+exp1 [6798 B]
Get:27 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libapache-pom-java all 29-2 [5284 B]
Get:28 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libatinject-jsr330-api-java all 1.0+ds1-5 [5348 B]
Get:29 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libatk1.0-0t64 amd64 2.52.0-1build1 [55.3 kB]
Get:30 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libatk-bridge2.0-0t64 amd64 2.52.0-1build1 [66.0 kB]
Get:31 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libglvnd0 amd64 1.7.0-1build1 [69.6 kB]
Get:32 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libx11-xcb1 amd64 2:1.8.7-1build1 [7800 B]
Get:33 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-dri3-0 amd64 1.15-1ubuntu2 [7142 B]
Get:34 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-glx0 amd64 1.15-1ubuntu2 [24.8 kB]
Get:35 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-present0 amd64 1.15-1ubuntu2 [5676 B]
Get:36 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-shm0 amd64 1.15-1ubuntu2 [5756 B]
Get:37 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-xfixes0 amd64 1.15-1ubuntu2 [10.2 kB]
Get:38 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxxf86vm1 amd64 1:1.1.4-1build4 [9282 B]
Get:39 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libpciaccess0 amd64 0.17-3ubuntu0.24.04.2 [18.9 kB]
Get:40 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libdrm-intel1 amd64 2.4.125-1ubuntu0.1~24.04.1 [63.8 kB]
Get:41 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libllvm20 amd64 1:20.1.2-0ubuntu1~24.04.2 [30.6 MB]
Get:42 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-randr0 amd64 1.15-1ubuntu2 [17.9 kB]
Get:43 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-sync1 amd64 1.15-1ubuntu2 [9312 B]
Get:44 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxshmfence1 amd64 1.3-1build5 [4764 B]
Get:45 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 mesa-libgallium amd64 25.2.8-0ubuntu0.24.04.1 [10.8 MB]
Get:46 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgbm1 amd64 25.2.8-0ubuntu0.24.04.1 [34.2 kB]
Get:47 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libvulkan1 amd64 1.3.275.0-1build1 [142 kB]
Get:48 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgl1-mesa-dri amd64 25.2.8-0ubuntu0.24.04.1 [37.9 kB]
Get:49 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libglx-mesa0 amd64 25.2.8-0ubuntu0.24.04.1 [110 kB]
Get:50 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libglx0 amd64 1.7.0-1build1 [38.6 kB]
Get:51 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libgl1 amd64 1.7.0-1build1 [102 kB]
Get:52 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libice6 amd64 2:1.0.10-1build3 [41.4 kB]
Get:53 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libsm6 amd64 2:1.2.3-1build3 [15.7 kB]
Get:54 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxt6t64 amd64 1:1.2.1-1.2build1 [171 kB]
Get:55 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxmu6 amd64 2:1.1.3-3build2 [47.6 kB]
Get:56 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxaw7 amd64 2:1.0.14-1build2 [187 kB]
Get:57 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-shape0 amd64 1.15-1ubuntu2 [6100 B]
Get:58 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcomposite1 amd64 1:0.4.5-1build3 [6320 B]
Get:59 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxrender1 amd64 1:0.9.10-1.1build1 [19.0 kB]
Get:60 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxft2 amd64 2.3.6-1build1 [45.3 kB]
Get:61 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxinerama1 amd64 2:1.1.4-3build1 [6396 B]
Get:62 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxkbfile1 amd64 1:1.1.0-1build4 [70.0 kB]
Get:63 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxrandr2 amd64 2:1.5.2-2build1 [19.7 kB]
Get:64 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxv1 amd64 2:1.0.11-1.1build1 [10.7 kB]
Get:65 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxxf86dga1 amd64 2:1.1.5-1build1 [11.6 kB]
Get:66 http://mirrors.digitalocean.com/ubuntu noble/main amd64 x11-utils amd64 7.7+6build2 [189 kB]
Get:67 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libatk-wrapper-java all 0.40.0-3build2 [54.3 kB]
Get:68 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libatk-wrapper-java-jni amd64 0.40.0-3build2 [46.4 kB]
Get:69 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libavahi-common-data amd64 0.8-13ubuntu6.2 [30.1 kB]
Get:70 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libavahi-common3 amd64 0.8-13ubuntu6.2 [23.4 kB]
Get:71 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libavahi-client3 amd64 0.8-13ubuntu6.2 [26.8 kB]
Get:72 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpixman-1-0 amd64 0.42.2-1build1 [279 kB]
Get:73 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb-render0 amd64 1.15-1ubuntu2 [16.2 kB]
Get:74 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libcairo2 amd64 1.18.0-3build1 [566 kB]
Get:75 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libcairo-gobject2 amd64 1.18.0-3build1 [127 kB]
Get:76 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libgeronimo-interceptor-3.0-spec-java all 1.0.1-4fakesync [8616 B]
Get:77 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libcdi-api-java all 1.2-3 [54.3 kB]
Get:78 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libcommons-cli-java all 1.6.0-1 [59.9 kB]
Get:79 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libcommons-parent-java all 56-1 [10.7 kB]
Get:80 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libcommons-io-java all 2.11.0-2 [297 kB]
Get:81 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libcommons-lang3-java all 3.14.0-1 [586 kB]
Get:82 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libcups2t64 amd64 2.4.7-1.2ubuntu7.13 [273 kB]
Get:83 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libdatrie1 amd64 0.2.13-3build1 [19.0 kB]
Get:84 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libjsr305-java all 0.1~+svn49-11 [27.0 kB]
Get:85 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libguava-java all 32.0.1-1 [2692 kB]
Get:86 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 liberror-prone-java all 2.18.0-1 [22.5 kB]
Get:87 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgtk2.0-common all 2.24.33-4ubuntu1.1 [127 kB]
Get:88 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libgraphite2-3 amd64 1.3.14-2build1 [73.0 kB]
Get:89 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libharfbuzz0b amd64 8.3.0-2build2 [469 kB]
Get:90 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libthai-data all 0.1.29-2build1 [158 kB]
Get:91 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libthai0 amd64 0.1.29-2build1 [18.9 kB]
Get:92 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpango-1.0-0 amd64 1.52.1+ds-1build1 [231 kB]
Get:93 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpangoft2-1.0-0 amd64 1.52.1+ds-1build1 [42.5 kB]
Get:94 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpangocairo-1.0-0 amd64 1.52.1+ds-1build1 [28.8 kB]
Get:95 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxfixes3 amd64 1:6.0.0-2build1 [10.8 kB]
Get:96 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcursor1 amd64 1:1.2.1-1build1 [20.7 kB]
Get:97 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxdamage1 amd64 1:1.1.6-1build1 [6150 B]
Get:98 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgtk2.0-0t64 amd64 2.24.33-4ubuntu1.1 [2006 kB]
Get:99 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgail18t64 amd64 2.24.33-4ubuntu1.1 [15.9 kB]
Get:100 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgail-common amd64 2.24.33-4ubuntu1.1 [126 kB]
Get:101 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgdk-pixbuf2.0-bin amd64 2.42.10+dfsg-3ubuntu3.3 [13.9 kB]
Get:102 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libgeronimo-annotation-1.3-spec-java all 1.3-1 [11.2 kB]
Get:103 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libgif7 amd64 5.2.2-1ubuntu1 [35.2 kB]
Get:104 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 libgtk2.0-bin amd64 2.24.33-4ubuntu1.1 [7954 B]
Get:105 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libaopalliance-java all 20070526-7 [8166 B]
Get:106 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libguice-java all 4.2.3-2 [1434 kB]
Get:107 http://mirrors.digitalocean.com/ubuntu noble/main amd64 xorg-sgml-doctools all 1:1.11-1.1 [10.9 kB]
Get:108 http://mirrors.digitalocean.com/ubuntu noble/main amd64 x11proto-dev all 2023.2-1 [602 kB]
Get:109 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libice-dev amd64 2:1.0.10-1build3 [51.0 kB]
Get:110 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libjansi-java all 2.4.1-2 [83.0 kB]
Get:111 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 liblcms2-2 amd64 2.14-2ubuntu0.1 [161 kB]
Get:112 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libmaven-parent-java all 35-1 [5810 B]
Get:113 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-utils2-java all 3.4.2-1 [256 kB]
Get:114 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libwagon-provider-api-java all 3.5.3-1 [47.9 kB]
Get:115 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libmaven-resolver-java all 1.6.3-1 [544 kB]
Get:116 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libmaven-shared-utils-java all 3.3.4-1 [137 kB]
Get:117 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-cipher-java all 2.0-1 [14.7 kB]
Get:118 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-classworlds-java all 2.7.0-1 [50.0 kB]
Get:119 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-component-annotations-java all 2.1.1-1 [6550 B]
Get:120 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-interpolation-java all 1.26-1 [76.8 kB]
Get:121 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libplexus-sec-dispatcher-java all 2.0-3 [28.1 kB]
Get:122 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libslf4j-java all 1.7.32-1 [141 kB]
Get:123 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libsisu-inject-java all 0.3.4-2 [347 kB]
Get:124 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libsisu-plexus-java all 0.3.4-3 [181 kB]
Get:125 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libmaven3-core-java all 3.8.7-2 [1565 kB]
Get:126 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpcsclite1 amd64 2.0.3-1build1 [21.4 kB]
Get:127 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libpthread-stubs0-dev amd64 0.4-1build3 [4746 B]
Get:128 http://mirrors.digitalocean.com/ubuntu noble/main amd64 librsvg2-2 amd64 2.58.0+dfsg-1build1 [2135 kB]
Get:129 http://mirrors.digitalocean.com/ubuntu noble/main amd64 librsvg2-common amd64 2.58.0+dfsg-1build1 [11.8 kB]
Get:130 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libsm-dev amd64 2:1.2.3-1build3 [17.8 kB]
Get:131 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libwagon-file-java all 3.5.3-1 [7918 B]
Get:132 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 libwagon-http-shaded-java all 3.5.3-1 [1332 kB]
Get:133 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libwayland-client0 amd64 1.22.0-2.1build1 [26.4 kB]
Get:134 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxau-dev amd64 1:1.0.9-1build6 [9570 B]
Get:135 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxdmcp-dev amd64 1:1.1.3-0ubuntu6 [26.5 kB]
Get:136 http://mirrors.digitalocean.com/ubuntu noble/main amd64 xtrans-dev all 1.4.0-1 [68.9 kB]
Get:137 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxcb1-dev amd64 1.15-1ubuntu2 [85.8 kB]
Get:138 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libx11-dev amd64 2:1.8.7-1build1 [732 kB]
Get:139 http://mirrors.digitalocean.com/ubuntu noble/main amd64 libxt-dev amd64 1:1.2.1-1.2build1 [394 kB]
Get:140 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 openjdk-17-jre-headless amd64 17.0.19+10-1~24.04.2 [48.1 MB]
Get:141 http://mirrors.digitalocean.com/ubuntu noble/universe amd64 maven all 3.8.7-2 [18.3 kB]
Get:142 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 mesa-vulkan-drivers amd64 25.2.8-0ubuntu0.24.04.1 [17.5 MB]
Get:143 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 openjdk-17-jre amd64 17.0.19+10-1~24.04.2 [234 kB]
Get:144 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 openjdk-17-jdk-headless amd64 17.0.19+10-1~24.04.2 [71.6 MB]
Get:145 http://mirrors.digitalocean.com/ubuntu noble-updates/main amd64 openjdk-17-jdk amd64 17.0.19+10-1~24.04.2 [2479 kB]
dpkg-preconfigure: unable to re-open stdin: No such file or directory
Fetched 208 MB in 7s (30.7 MB/s)
Selecting previously unselected package libgdk-pixbuf2.0-common.
(Reading database ... (Reading database ... 5%(Reading database ... 10%(Reading database ... 15%(Reading database ... 20%(Reading database ... 25%(Reading database ... 30%(Reading database ... 35%(Reading database ... 40%(Reading database ... 45%(Reading database ... 50%(Reading database ... 55%(Reading database ... 60%(Reading database ... 65%(Reading database ... 70%(Reading database ... 75%(Reading database ... 80%(Reading database ... 85%(Reading database ... 90%(Reading database ... 95%(Reading database ... 100%(Reading database ... 114525 files and directories currently installed.)
Preparing to unpack .../000-libgdk-pixbuf2.0-common_2.42.10+dfsg-3ubuntu3.3_all.deb ...
Unpacking libgdk-pixbuf2.0-common (2.42.10+dfsg-3ubuntu3.3) ...
Selecting previously unselected package libgdk-pixbuf-2.0-0:amd64.
Preparing to unpack .../001-libgdk-pixbuf-2.0-0_2.42.10+dfsg-3ubuntu3.3_amd64.deb ...
Unpacking libgdk-pixbuf-2.0-0:amd64 (2.42.10+dfsg-3ubuntu3.3) ...
Selecting previously unselected package gtk-update-icon-cache.
Preparing to unpack .../002-gtk-update-icon-cache_3.24.41-4ubuntu1.3_amd64.deb ...
Unpacking gtk-update-icon-cache (3.24.41-4ubuntu1.3) ...
Selecting previously unselected package hicolor-icon-theme.
Preparing to unpack .../003-hicolor-icon-theme_0.17-2_all.deb ...
Unpacking hicolor-icon-theme (0.17-2) ...
Selecting previously unselected package humanity-icon-theme.
Preparing to unpack .../004-humanity-icon-theme_0.6.16_all.deb ...
Unpacking humanity-icon-theme (0.6.16) ...
Selecting previously unselected package ubuntu-mono.
Preparing to unpack .../005-ubuntu-mono_24.04-0ubuntu1_all.deb ...
Unpacking ubuntu-mono (24.04-0ubuntu1) ...
Selecting previously unselected package adwaita-icon-theme.
Preparing to unpack .../006-adwaita-icon-theme_46.0-1_all.deb ...
Unpacking adwaita-icon-theme (46.0-1) ...
Selecting previously unselected package alsa-topology-conf.
Preparing to unpack .../007-alsa-topology-conf_1.2.5.1-2_all.deb ...
Unpacking alsa-topology-conf (1.2.5.1-2) ...
Selecting previously unselected package libasound2-data.
Preparing to unpack .../008-libasound2-data_1.2.11-1ubuntu0.2_all.deb ...
Unpacking libasound2-data (1.2.11-1ubuntu0.2) ...
Selecting previously unselected package libasound2t64:amd64.
Preparing to unpack .../009-libasound2t64_1.2.11-1ubuntu0.2_amd64.deb ...
Unpacking libasound2t64:amd64 (1.2.11-1ubuntu0.2) ...
Selecting previously unselected package alsa-ucm-conf.
Preparing to unpack .../010-alsa-ucm-conf_1.2.10-1ubuntu5.11_all.deb ...
Unpacking alsa-ucm-conf (1.2.10-1ubuntu5.11) ...
Selecting previously unselected package at-spi2-common.
Preparing to unpack .../011-at-spi2-common_2.52.0-1build1_all.deb ...
Unpacking at-spi2-common (2.52.0-1build1) ...
Selecting previously unselected package libxi6:amd64.
Preparing to unpack .../012-libxi6_2%3a1.8.1-1build1_amd64.deb ...
Unpacking libxi6:amd64 (2:1.8.1-1build1) ...
Selecting previously unselected package libatspi2.0-0t64:amd64.
Preparing to unpack .../013-libatspi2.0-0t64_2.52.0-1build1_amd64.deb ...
Unpacking libatspi2.0-0t64:amd64 (2.52.0-1build1) ...
Selecting previously unselected package x11-common.
Preparing to unpack .../014-x11-common_1%3a7.7+23ubuntu3_all.deb ...
Unpacking x11-common (1:7.7+23ubuntu3) ...
Selecting previously unselected package libxtst6:amd64.
Preparing to unpack .../015-libxtst6_2%3a1.2.3-1.1build1_amd64.deb ...
Unpacking libxtst6:amd64 (2:1.2.3-1.1build1) ...
Selecting previously unselected package libdconf1:amd64.
Preparing to unpack .../016-libdconf1_0.40.0-4ubuntu0.1_amd64.deb ...
Unpacking libdconf1:amd64 (0.40.0-4ubuntu0.1) ...
Selecting previously unselected package dconf-service.
Preparing to unpack .../017-dconf-service_0.40.0-4ubuntu0.1_amd64.deb ...
Unpacking dconf-service (0.40.0-4ubuntu0.1) ...
Selecting previously unselected package dconf-gsettings-backend:amd64.
Preparing to unpack .../018-dconf-gsettings-backend_0.40.0-4ubuntu0.1_amd64.deb ...
Unpacking dconf-gsettings-backend:amd64 (0.40.0-4ubuntu0.1) ...
Selecting previously unselected package session-migration.
Preparing to unpack .../019-session-migration_0.3.9build1_amd64.deb ...
Unpacking session-migration (0.3.9build1) ...
Selecting previously unselected package gsettings-desktop-schemas.
Preparing to unpack .../020-gsettings-desktop-schemas_46.1-0ubuntu1_all.deb ...
Unpacking gsettings-desktop-schemas (46.1-0ubuntu1) ...
Selecting previously unselected package at-spi2-core.
Preparing to unpack .../021-at-spi2-core_2.52.0-1build1_amd64.deb ...
Unpacking at-spi2-core (2.52.0-1build1) ...
Selecting previously unselected package ca-certificates-java.
Preparing to unpack .../022-ca-certificates-java_20240118_all.deb ...
Unpacking ca-certificates-java (20240118) ...
Selecting previously unselected package fontconfig.
Preparing to unpack .../023-fontconfig_2.15.0-1.1ubuntu2_amd64.deb ...
Unpacking fontconfig (2.15.0-1.1ubuntu2) ...
Selecting previously unselected package fonts-dejavu-extra.
Preparing to unpack .../024-fonts-dejavu-extra_2.37-8_all.deb ...
Unpacking fonts-dejavu-extra (2.37-8) ...
Selecting previously unselected package java-common.
Preparing to unpack .../025-java-common_0.75+exp1_all.deb ...
Unpacking java-common (0.75+exp1) ...
Selecting previously unselected package libapache-pom-java.
Preparing to unpack .../026-libapache-pom-java_29-2_all.deb ...
Unpacking libapache-pom-java (29-2) ...
Selecting previously unselected package libatinject-jsr330-api-java.
Preparing to unpack .../027-libatinject-jsr330-api-java_1.0+ds1-5_all.deb ...
Unpacking libatinject-jsr330-api-java (1.0+ds1-5) ...
Selecting previously unselected package libatk1.0-0t64:amd64.
Preparing to unpack .../028-libatk1.0-0t64_2.52.0-1build1_amd64.deb ...
Unpacking libatk1.0-0t64:amd64 (2.52.0-1build1) ...
Selecting previously unselected package libatk-bridge2.0-0t64:amd64.
Preparing to unpack .../029-libatk-bridge2.0-0t64_2.52.0-1build1_amd64.deb ...
Unpacking libatk-bridge2.0-0t64:amd64 (2.52.0-1build1) ...
Selecting previously unselected package libglvnd0:amd64.
Preparing to unpack .../030-libglvnd0_1.7.0-1build1_amd64.deb ...
Unpacking libglvnd0:amd64 (1.7.0-1build1) ...
Selecting previously unselected package libx11-xcb1:amd64.
Preparing to unpack .../031-libx11-xcb1_2%3a1.8.7-1build1_amd64.deb ...
Unpacking libx11-xcb1:amd64 (2:1.8.7-1build1) ...
Selecting previously unselected package libxcb-dri3-0:amd64.
Preparing to unpack .../032-libxcb-dri3-0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-dri3-0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcb-glx0:amd64.
Preparing to unpack .../033-libxcb-glx0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-glx0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcb-present0:amd64.
Preparing to unpack .../034-libxcb-present0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-present0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcb-shm0:amd64.
Preparing to unpack .../035-libxcb-shm0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-shm0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcb-xfixes0:amd64.
Preparing to unpack .../036-libxcb-xfixes0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-xfixes0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxxf86vm1:amd64.
Preparing to unpack .../037-libxxf86vm1_1%3a1.1.4-1build4_amd64.deb ...
Unpacking libxxf86vm1:amd64 (1:1.1.4-1build4) ...
Selecting previously unselected package libpciaccess0:amd64.
Preparing to unpack .../038-libpciaccess0_0.17-3ubuntu0.24.04.2_amd64.deb ...
Unpacking libpciaccess0:amd64 (0.17-3ubuntu0.24.04.2) ...
Selecting previously unselected package libdrm-intel1:amd64.
Preparing to unpack .../039-libdrm-intel1_2.4.125-1ubuntu0.1~24.04.1_amd64.deb ...
Unpacking libdrm-intel1:amd64 (2.4.125-1ubuntu0.1~24.04.1) ...
Selecting previously unselected package libllvm20:amd64.
Preparing to unpack .../040-libllvm20_1%3a20.1.2-0ubuntu1~24.04.2_amd64.deb ...
Unpacking libllvm20:amd64 (1:20.1.2-0ubuntu1~24.04.2) ...
Selecting previously unselected package libxcb-randr0:amd64.
Preparing to unpack .../041-libxcb-randr0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-randr0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcb-sync1:amd64.
Preparing to unpack .../042-libxcb-sync1_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-sync1:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxshmfence1:amd64.
Preparing to unpack .../043-libxshmfence1_1.3-1build5_amd64.deb ...
Unpacking libxshmfence1:amd64 (1.3-1build5) ...
Selecting previously unselected package mesa-libgallium:amd64.
Preparing to unpack .../044-mesa-libgallium_25.2.8-0ubuntu0.24.04.1_amd64.deb ...
Unpacking mesa-libgallium:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Selecting previously unselected package libgbm1:amd64.
Preparing to unpack .../045-libgbm1_25.2.8-0ubuntu0.24.04.1_amd64.deb ...
Unpacking libgbm1:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Selecting previously unselected package libvulkan1:amd64.
Preparing to unpack .../046-libvulkan1_1.3.275.0-1build1_amd64.deb ...
Unpacking libvulkan1:amd64 (1.3.275.0-1build1) ...
Selecting previously unselected package libgl1-mesa-dri:amd64.
Preparing to unpack .../047-libgl1-mesa-dri_25.2.8-0ubuntu0.24.04.1_amd64.deb ...
Unpacking libgl1-mesa-dri:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Selecting previously unselected package libglx-mesa0:amd64.
Preparing to unpack .../048-libglx-mesa0_25.2.8-0ubuntu0.24.04.1_amd64.deb ...
Unpacking libglx-mesa0:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Selecting previously unselected package libglx0:amd64.
Preparing to unpack .../049-libglx0_1.7.0-1build1_amd64.deb ...
Unpacking libglx0:amd64 (1.7.0-1build1) ...
Selecting previously unselected package libgl1:amd64.
Preparing to unpack .../050-libgl1_1.7.0-1build1_amd64.deb ...
Unpacking libgl1:amd64 (1.7.0-1build1) ...
Selecting previously unselected package libice6:amd64.
Preparing to unpack .../051-libice6_2%3a1.0.10-1build3_amd64.deb ...
Unpacking libice6:amd64 (2:1.0.10-1build3) ...
Selecting previously unselected package libsm6:amd64.
Preparing to unpack .../052-libsm6_2%3a1.2.3-1build3_amd64.deb ...
Unpacking libsm6:amd64 (2:1.2.3-1build3) ...
Selecting previously unselected package libxt6t64:amd64.
Preparing to unpack .../053-libxt6t64_1%3a1.2.1-1.2build1_amd64.deb ...
Unpacking libxt6t64:amd64 (1:1.2.1-1.2build1) ...
Selecting previously unselected package libxmu6:amd64.
Preparing to unpack .../054-libxmu6_2%3a1.1.3-3build2_amd64.deb ...
Unpacking libxmu6:amd64 (2:1.1.3-3build2) ...
Selecting previously unselected package libxaw7:amd64.
Preparing to unpack .../055-libxaw7_2%3a1.0.14-1build2_amd64.deb ...
Unpacking libxaw7:amd64 (2:1.0.14-1build2) ...
Selecting previously unselected package libxcb-shape0:amd64.
Preparing to unpack .../056-libxcb-shape0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-shape0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libxcomposite1:amd64.
Preparing to unpack .../057-libxcomposite1_1%3a0.4.5-1build3_amd64.deb ...
Unpacking libxcomposite1:amd64 (1:0.4.5-1build3) ...
Selecting previously unselected package libxrender1:amd64.
Preparing to unpack .../058-libxrender1_1%3a0.9.10-1.1build1_amd64.deb ...
Unpacking libxrender1:amd64 (1:0.9.10-1.1build1) ...
Selecting previously unselected package libxft2:amd64.
Preparing to unpack .../059-libxft2_2.3.6-1build1_amd64.deb ...
Unpacking libxft2:amd64 (2.3.6-1build1) ...
Selecting previously unselected package libxinerama1:amd64.
Preparing to unpack .../060-libxinerama1_2%3a1.1.4-3build1_amd64.deb ...
Unpacking libxinerama1:amd64 (2:1.1.4-3build1) ...
Selecting previously unselected package libxkbfile1:amd64.
Preparing to unpack .../061-libxkbfile1_1%3a1.1.0-1build4_amd64.deb ...
Unpacking libxkbfile1:amd64 (1:1.1.0-1build4) ...
Selecting previously unselected package libxrandr2:amd64.
Preparing to unpack .../062-libxrandr2_2%3a1.5.2-2build1_amd64.deb ...
Unpacking libxrandr2:amd64 (2:1.5.2-2build1) ...
Selecting previously unselected package libxv1:amd64.
Preparing to unpack .../063-libxv1_2%3a1.0.11-1.1build1_amd64.deb ...
Unpacking libxv1:amd64 (2:1.0.11-1.1build1) ...
Selecting previously unselected package libxxf86dga1:amd64.
Preparing to unpack .../064-libxxf86dga1_2%3a1.1.5-1build1_amd64.deb ...
Unpacking libxxf86dga1:amd64 (2:1.1.5-1build1) ...
Selecting previously unselected package x11-utils.
Preparing to unpack .../065-x11-utils_7.7+6build2_amd64.deb ...
Unpacking x11-utils (7.7+6build2) ...
Selecting previously unselected package libatk-wrapper-java.
Preparing to unpack .../066-libatk-wrapper-java_0.40.0-3build2_all.deb ...
Unpacking libatk-wrapper-java (0.40.0-3build2) ...
Selecting previously unselected package libatk-wrapper-java-jni:amd64.
Preparing to unpack .../067-libatk-wrapper-java-jni_0.40.0-3build2_amd64.deb ...
Unpacking libatk-wrapper-java-jni:amd64 (0.40.0-3build2) ...
Selecting previously unselected package libavahi-common-data:amd64.
Preparing to unpack .../068-libavahi-common-data_0.8-13ubuntu6.2_amd64.deb ...
Unpacking libavahi-common-data:amd64 (0.8-13ubuntu6.2) ...
Selecting previously unselected package libavahi-common3:amd64.
Preparing to unpack .../069-libavahi-common3_0.8-13ubuntu6.2_amd64.deb ...
Unpacking libavahi-common3:amd64 (0.8-13ubuntu6.2) ...
Selecting previously unselected package libavahi-client3:amd64.
Preparing to unpack .../070-libavahi-client3_0.8-13ubuntu6.2_amd64.deb ...
Unpacking libavahi-client3:amd64 (0.8-13ubuntu6.2) ...
Selecting previously unselected package libpixman-1-0:amd64.
Preparing to unpack .../071-libpixman-1-0_0.42.2-1build1_amd64.deb ...
Unpacking libpixman-1-0:amd64 (0.42.2-1build1) ...
Selecting previously unselected package libxcb-render0:amd64.
Preparing to unpack .../072-libxcb-render0_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb-render0:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libcairo2:amd64.
Preparing to unpack .../073-libcairo2_1.18.0-3build1_amd64.deb ...
Unpacking libcairo2:amd64 (1.18.0-3build1) ...
Selecting previously unselected package libcairo-gobject2:amd64.
Preparing to unpack .../074-libcairo-gobject2_1.18.0-3build1_amd64.deb ...
Unpacking libcairo-gobject2:amd64 (1.18.0-3build1) ...
Selecting previously unselected package libgeronimo-interceptor-3.0-spec-java.
Preparing to unpack .../075-libgeronimo-interceptor-3.0-spec-java_1.0.1-4fakesync_all.deb ...
Unpacking libgeronimo-interceptor-3.0-spec-java (1.0.1-4fakesync) ...
Selecting previously unselected package libcdi-api-java.
Preparing to unpack .../076-libcdi-api-java_1.2-3_all.deb ...
Unpacking libcdi-api-java (1.2-3) ...
Selecting previously unselected package libcommons-cli-java.
Preparing to unpack .../077-libcommons-cli-java_1.6.0-1_all.deb ...
Unpacking libcommons-cli-java (1.6.0-1) ...
Selecting previously unselected package libcommons-parent-java.
Preparing to unpack .../078-libcommons-parent-java_56-1_all.deb ...
Unpacking libcommons-parent-java (56-1) ...
Selecting previously unselected package libcommons-io-java.
Preparing to unpack .../079-libcommons-io-java_2.11.0-2_all.deb ...
Unpacking libcommons-io-java (2.11.0-2) ...
Selecting previously unselected package libcommons-lang3-java.
Preparing to unpack .../080-libcommons-lang3-java_3.14.0-1_all.deb ...
Unpacking libcommons-lang3-java (3.14.0-1) ...
Selecting previously unselected package libcups2t64:amd64.
Preparing to unpack .../081-libcups2t64_2.4.7-1.2ubuntu7.13_amd64.deb ...
Unpacking libcups2t64:amd64 (2.4.7-1.2ubuntu7.13) ...
Selecting previously unselected package libdatrie1:amd64.
Preparing to unpack .../082-libdatrie1_0.2.13-3build1_amd64.deb ...
Unpacking libdatrie1:amd64 (0.2.13-3build1) ...
Selecting previously unselected package libjsr305-java.
Preparing to unpack .../083-libjsr305-java_0.1~+svn49-11_all.deb ...
Unpacking libjsr305-java (0.1~+svn49-11) ...
Selecting previously unselected package libguava-java.
Preparing to unpack .../084-libguava-java_32.0.1-1_all.deb ...
Unpacking libguava-java (32.0.1-1) ...
Selecting previously unselected package liberror-prone-java.
Preparing to unpack .../085-liberror-prone-java_2.18.0-1_all.deb ...
Unpacking liberror-prone-java (2.18.0-1) ...
Selecting previously unselected package libgtk2.0-common.
Preparing to unpack .../086-libgtk2.0-common_2.24.33-4ubuntu1.1_all.deb ...
Unpacking libgtk2.0-common (2.24.33-4ubuntu1.1) ...
Selecting previously unselected package libgraphite2-3:amd64.
Preparing to unpack .../087-libgraphite2-3_1.3.14-2build1_amd64.deb ...
Unpacking libgraphite2-3:amd64 (1.3.14-2build1) ...
Selecting previously unselected package libharfbuzz0b:amd64.
Preparing to unpack .../088-libharfbuzz0b_8.3.0-2build2_amd64.deb ...
Unpacking libharfbuzz0b:amd64 (8.3.0-2build2) ...
Selecting previously unselected package libthai-data.
Preparing to unpack .../089-libthai-data_0.1.29-2build1_all.deb ...
Unpacking libthai-data (0.1.29-2build1) ...
Selecting previously unselected package libthai0:amd64.
Preparing to unpack .../090-libthai0_0.1.29-2build1_amd64.deb ...
Unpacking libthai0:amd64 (0.1.29-2build1) ...
Selecting previously unselected package libpango-1.0-0:amd64.
Preparing to unpack .../091-libpango-1.0-0_1.52.1+ds-1build1_amd64.deb ...
Unpacking libpango-1.0-0:amd64 (1.52.1+ds-1build1) ...
Selecting previously unselected package libpangoft2-1.0-0:amd64.
Preparing to unpack .../092-libpangoft2-1.0-0_1.52.1+ds-1build1_amd64.deb ...
Unpacking libpangoft2-1.0-0:amd64 (1.52.1+ds-1build1) ...
Selecting previously unselected package libpangocairo-1.0-0:amd64.
Preparing to unpack .../093-libpangocairo-1.0-0_1.52.1+ds-1build1_amd64.deb ...
Unpacking libpangocairo-1.0-0:amd64 (1.52.1+ds-1build1) ...
Selecting previously unselected package libxfixes3:amd64.
Preparing to unpack .../094-libxfixes3_1%3a6.0.0-2build1_amd64.deb ...
Unpacking libxfixes3:amd64 (1:6.0.0-2build1) ...
Selecting previously unselected package libxcursor1:amd64.
Preparing to unpack .../095-libxcursor1_1%3a1.2.1-1build1_amd64.deb ...
Unpacking libxcursor1:amd64 (1:1.2.1-1build1) ...
Selecting previously unselected package libxdamage1:amd64.
Preparing to unpack .../096-libxdamage1_1%3a1.1.6-1build1_amd64.deb ...
Unpacking libxdamage1:amd64 (1:1.1.6-1build1) ...
Selecting previously unselected package libgtk2.0-0t64:amd64.
Preparing to unpack .../097-libgtk2.0-0t64_2.24.33-4ubuntu1.1_amd64.deb ...
Unpacking libgtk2.0-0t64:amd64 (2.24.33-4ubuntu1.1) ...
Selecting previously unselected package libgail18t64:amd64.
Preparing to unpack .../098-libgail18t64_2.24.33-4ubuntu1.1_amd64.deb ...
Unpacking libgail18t64:amd64 (2.24.33-4ubuntu1.1) ...
Selecting previously unselected package libgail-common:amd64.
Preparing to unpack .../099-libgail-common_2.24.33-4ubuntu1.1_amd64.deb ...
Unpacking libgail-common:amd64 (2.24.33-4ubuntu1.1) ...
Selecting previously unselected package libgdk-pixbuf2.0-bin.
Preparing to unpack .../100-libgdk-pixbuf2.0-bin_2.42.10+dfsg-3ubuntu3.3_amd64.deb ...
Unpacking libgdk-pixbuf2.0-bin (2.42.10+dfsg-3ubuntu3.3) ...
Selecting previously unselected package libgeronimo-annotation-1.3-spec-java.
Preparing to unpack .../101-libgeronimo-annotation-1.3-spec-java_1.3-1_all.deb ...
Unpacking libgeronimo-annotation-1.3-spec-java (1.3-1) ...
Selecting previously unselected package libgif7:amd64.
Preparing to unpack .../102-libgif7_5.2.2-1ubuntu1_amd64.deb ...
Unpacking libgif7:amd64 (5.2.2-1ubuntu1) ...
Selecting previously unselected package libgtk2.0-bin.
Preparing to unpack .../103-libgtk2.0-bin_2.24.33-4ubuntu1.1_amd64.deb ...
Unpacking libgtk2.0-bin (2.24.33-4ubuntu1.1) ...
Selecting previously unselected package libaopalliance-java.
Preparing to unpack .../104-libaopalliance-java_20070526-7_all.deb ...
Unpacking libaopalliance-java (20070526-7) ...
Selecting previously unselected package libguice-java.
Preparing to unpack .../105-libguice-java_4.2.3-2_all.deb ...
Unpacking libguice-java (4.2.3-2) ...
Selecting previously unselected package xorg-sgml-doctools.
Preparing to unpack .../106-xorg-sgml-doctools_1%3a1.11-1.1_all.deb ...
Unpacking xorg-sgml-doctools (1:1.11-1.1) ...
Selecting previously unselected package x11proto-dev.
Preparing to unpack .../107-x11proto-dev_2023.2-1_all.deb ...
Unpacking x11proto-dev (2023.2-1) ...
Selecting previously unselected package libice-dev:amd64.
Preparing to unpack .../108-libice-dev_2%3a1.0.10-1build3_amd64.deb ...
Unpacking libice-dev:amd64 (2:1.0.10-1build3) ...
Selecting previously unselected package libjansi-java.
Preparing to unpack .../109-libjansi-java_2.4.1-2_all.deb ...
Unpacking libjansi-java (2.4.1-2) ...
Selecting previously unselected package liblcms2-2:amd64.
Preparing to unpack .../110-liblcms2-2_2.14-2ubuntu0.1_amd64.deb ...
Unpacking liblcms2-2:amd64 (2.14-2ubuntu0.1) ...
Selecting previously unselected package libmaven-parent-java.
Preparing to unpack .../111-libmaven-parent-java_35-1_all.deb ...
Unpacking libmaven-parent-java (35-1) ...
Selecting previously unselected package libplexus-utils2-java.
Preparing to unpack .../112-libplexus-utils2-java_3.4.2-1_all.deb ...
Unpacking libplexus-utils2-java (3.4.2-1) ...
Selecting previously unselected package libwagon-provider-api-java.
Preparing to unpack .../113-libwagon-provider-api-java_3.5.3-1_all.deb ...
Unpacking libwagon-provider-api-java (3.5.3-1) ...
Selecting previously unselected package libmaven-resolver-java.
Preparing to unpack .../114-libmaven-resolver-java_1.6.3-1_all.deb ...
Unpacking libmaven-resolver-java (1.6.3-1) ...
Selecting previously unselected package libmaven-shared-utils-java.
Preparing to unpack .../115-libmaven-shared-utils-java_3.3.4-1_all.deb ...
Unpacking libmaven-shared-utils-java (3.3.4-1) ...
Selecting previously unselected package libplexus-cipher-java.
Preparing to unpack .../116-libplexus-cipher-java_2.0-1_all.deb ...
Unpacking libplexus-cipher-java (2.0-1) ...
Selecting previously unselected package libplexus-classworlds-java.
Preparing to unpack .../117-libplexus-classworlds-java_2.7.0-1_all.deb ...
Unpacking libplexus-classworlds-java (2.7.0-1) ...
Selecting previously unselected package libplexus-component-annotations-java.
Preparing to unpack .../118-libplexus-component-annotations-java_2.1.1-1_all.deb ...
Unpacking libplexus-component-annotations-java (2.1.1-1) ...
Selecting previously unselected package libplexus-interpolation-java.
Preparing to unpack .../119-libplexus-interpolation-java_1.26-1_all.deb ...
Unpacking libplexus-interpolation-java (1.26-1) ...
Selecting previously unselected package libplexus-sec-dispatcher-java.
Preparing to unpack .../120-libplexus-sec-dispatcher-java_2.0-3_all.deb ...
Unpacking libplexus-sec-dispatcher-java (2.0-3) ...
Selecting previously unselected package libslf4j-java.
Preparing to unpack .../121-libslf4j-java_1.7.32-1_all.deb ...
Unpacking libslf4j-java (1.7.32-1) ...
Selecting previously unselected package libsisu-inject-java.
Preparing to unpack .../122-libsisu-inject-java_0.3.4-2_all.deb ...
Unpacking libsisu-inject-java (0.3.4-2) ...
Selecting previously unselected package libsisu-plexus-java.
Preparing to unpack .../123-libsisu-plexus-java_0.3.4-3_all.deb ...
Unpacking libsisu-plexus-java (0.3.4-3) ...
Selecting previously unselected package libmaven3-core-java.
Preparing to unpack .../124-libmaven3-core-java_3.8.7-2_all.deb ...
Unpacking libmaven3-core-java (3.8.7-2) ...
Selecting previously unselected package libpcsclite1:amd64.
Preparing to unpack .../125-libpcsclite1_2.0.3-1build1_amd64.deb ...
Unpacking libpcsclite1:amd64 (2.0.3-1build1) ...
Selecting previously unselected package libpthread-stubs0-dev:amd64.
Preparing to unpack .../126-libpthread-stubs0-dev_0.4-1build3_amd64.deb ...
Unpacking libpthread-stubs0-dev:amd64 (0.4-1build3) ...
Selecting previously unselected package librsvg2-2:amd64.
Preparing to unpack .../127-librsvg2-2_2.58.0+dfsg-1build1_amd64.deb ...
Unpacking librsvg2-2:amd64 (2.58.0+dfsg-1build1) ...
Selecting previously unselected package librsvg2-common:amd64.
Preparing to unpack .../128-librsvg2-common_2.58.0+dfsg-1build1_amd64.deb ...
Unpacking librsvg2-common:amd64 (2.58.0+dfsg-1build1) ...
Selecting previously unselected package libsm-dev:amd64.
Preparing to unpack .../129-libsm-dev_2%3a1.2.3-1build3_amd64.deb ...
Unpacking libsm-dev:amd64 (2:1.2.3-1build3) ...
Selecting previously unselected package libwagon-file-java.
Preparing to unpack .../130-libwagon-file-java_3.5.3-1_all.deb ...
Unpacking libwagon-file-java (3.5.3-1) ...
Selecting previously unselected package libwagon-http-shaded-java.
Preparing to unpack .../131-libwagon-http-shaded-java_3.5.3-1_all.deb ...
Unpacking libwagon-http-shaded-java (3.5.3-1) ...
Selecting previously unselected package libwayland-client0:amd64.
Preparing to unpack .../132-libwayland-client0_1.22.0-2.1build1_amd64.deb ...
Unpacking libwayland-client0:amd64 (1.22.0-2.1build1) ...
Selecting previously unselected package libxau-dev:amd64.
Preparing to unpack .../133-libxau-dev_1%3a1.0.9-1build6_amd64.deb ...
Unpacking libxau-dev:amd64 (1:1.0.9-1build6) ...
Selecting previously unselected package libxdmcp-dev:amd64.
Preparing to unpack .../134-libxdmcp-dev_1%3a1.1.3-0ubuntu6_amd64.deb ...
Unpacking libxdmcp-dev:amd64 (1:1.1.3-0ubuntu6) ...
Selecting previously unselected package xtrans-dev.
Preparing to unpack .../135-xtrans-dev_1.4.0-1_all.deb ...
Unpacking xtrans-dev (1.4.0-1) ...
Selecting previously unselected package libxcb1-dev:amd64.
Preparing to unpack .../136-libxcb1-dev_1.15-1ubuntu2_amd64.deb ...
Unpacking libxcb1-dev:amd64 (1.15-1ubuntu2) ...
Selecting previously unselected package libx11-dev:amd64.
Preparing to unpack .../137-libx11-dev_2%3a1.8.7-1build1_amd64.deb ...
Unpacking libx11-dev:amd64 (2:1.8.7-1build1) ...
Selecting previously unselected package libxt-dev:amd64.
Preparing to unpack .../138-libxt-dev_1%3a1.2.1-1.2build1_amd64.deb ...
Unpacking libxt-dev:amd64 (1:1.2.1-1.2build1) ...
Selecting previously unselected package openjdk-17-jre-headless:amd64.
Preparing to unpack .../139-openjdk-17-jre-headless_17.0.19+10-1~24.04.2_amd64.deb ...
Unpacking openjdk-17-jre-headless:amd64 (17.0.19+10-1~24.04.2) ...
Selecting previously unselected package maven.
Preparing to unpack .../140-maven_3.8.7-2_all.deb ...
Unpacking maven (3.8.7-2) ...
Selecting previously unselected package mesa-vulkan-drivers:amd64.
Preparing to unpack .../141-mesa-vulkan-drivers_25.2.8-0ubuntu0.24.04.1_amd64.deb ...
Unpacking mesa-vulkan-drivers:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Selecting previously unselected package openjdk-17-jre:amd64.
Preparing to unpack .../142-openjdk-17-jre_17.0.19+10-1~24.04.2_amd64.deb ...
Unpacking openjdk-17-jre:amd64 (17.0.19+10-1~24.04.2) ...
Selecting previously unselected package openjdk-17-jdk-headless:amd64.
Preparing to unpack .../143-openjdk-17-jdk-headless_17.0.19+10-1~24.04.2_amd64.deb ...
Unpacking openjdk-17-jdk-headless:amd64 (17.0.19+10-1~24.04.2) ...
Selecting previously unselected package openjdk-17-jdk:amd64.
Preparing to unpack .../144-openjdk-17-jdk_17.0.19+10-1~24.04.2_amd64.deb ...
Unpacking openjdk-17-jdk:amd64 (17.0.19+10-1~24.04.2) ...
Setting up libgraphite2-3:amd64 (1.3.14-2build1) ...
Setting up libxcb-dri3-0:amd64 (1.15-1ubuntu2) ...
Setting up liblcms2-2:amd64 (2.14-2ubuntu0.1) ...
Setting up libpixman-1-0:amd64 (0.42.2-1build1) ...
Setting up libx11-xcb1:amd64 (2:1.8.7-1build1) ...
Setting up libpciaccess0:amd64 (0.17-3ubuntu0.24.04.2) ...
Setting up libslf4j-java (1.7.32-1) ...
Setting up session-migration (0.3.9build1) ...
Created symlink /etc/systemd/user/graphical-session-pre.target.wants/session-migration.service → /usr/lib/systemd/user/session-migration.service.
Setting up fontconfig (2.15.0-1.1ubuntu2) ...
Regenerating fonts cache... done.
Setting up libplexus-utils2-java (3.4.2-1) ...
Setting up libplexus-classworlds-java (2.7.0-1) ...
Setting up libxdamage1:amd64 (1:1.1.6-1build1) ...
Setting up libxcb-xfixes0:amd64 (1.15-1ubuntu2) ...
Setting up libjsr305-java (0.1~+svn49-11) ...
Setting up hicolor-icon-theme (0.17-2) ...
Setting up libxi6:amd64 (2:1.8.1-1build1) ...
Setting up java-common (0.75+exp1) ...
Setting up libxrender1:amd64 (1:0.9.10-1.1build1) ...
Setting up libdatrie1:amd64 (0.2.13-3build1) ...
Setting up libaopalliance-java (20070526-7) ...
Setting up libcommons-cli-java (1.6.0-1) ...
Setting up libxcb-render0:amd64 (1.15-1ubuntu2) ...
Setting up libglvnd0:amd64 (1.7.0-1build1) ...
Setting up libxcb-glx0:amd64 (1.15-1ubuntu2) ...
Setting up libdrm-intel1:amd64 (2.4.125-1ubuntu0.1~24.04.1) ...
Setting up libgdk-pixbuf2.0-common (2.42.10+dfsg-3ubuntu3.3) ...
Setting up libxcb-shape0:amd64 (1.15-1ubuntu2) ...
Setting up x11-common (1:7.7+23ubuntu3) ...
Setting up libxxf86dga1:amd64 (2:1.1.5-1build1) ...
Setting up libplexus-component-annotations-java (2.1.1-1) ...
Setting up libxcb-shm0:amd64 (1.15-1ubuntu2) ...
Setting up libpthread-stubs0-dev:amd64 (0.4-1build3) ...
Setting up libcairo2:amd64 (1.18.0-3build1) ...
Setting up libxxf86vm1:amd64 (1:1.1.4-1build4) ...
Setting up libxcb-present0:amd64 (1.15-1ubuntu2) ...
Setting up libdconf1:amd64 (0.40.0-4ubuntu0.1) ...
Setting up libgeronimo-annotation-1.3-spec-java (1.3-1) ...
Setting up libgeronimo-interceptor-3.0-spec-java (1.0.1-4fakesync) ...
Setting up libasound2-data (1.2.11-1ubuntu0.2) ...
Setting up xtrans-dev (1.4.0-1) ...
Setting up libasound2t64:amd64 (1.2.11-1ubuntu0.2) ...
Setting up libxfixes3:amd64 (1:6.0.0-2build1) ...
Setting up libxcb-sync1:amd64 (1.15-1ubuntu2) ...
Setting up libjansi-java (2.4.1-2) ...
Setting up libapache-pom-java (29-2) ...
Setting up libavahi-common-data:amd64 (0.8-13ubuntu6.2) ...
Setting up libatinject-jsr330-api-java (1.0+ds1-5) ...
Setting up libatspi2.0-0t64:amd64 (2.52.0-1build1) ...
Setting up libxinerama1:amd64 (2:1.1.4-3build1) ...
Setting up libplexus-interpolation-java (1.26-1) ...
Setting up libxv1:amd64 (2:1.0.11-1.1build1) ...
Setting up libxrandr2:amd64 (2:1.5.2-2build1) ...
Setting up libllvm20:amd64 (1:20.1.2-0ubuntu1~24.04.2) ...
Setting up libpcsclite1:amd64 (2.0.3-1build1) ...
Setting up libvulkan1:amd64 (1.3.275.0-1build1) ...
Setting up libgif7:amd64 (5.2.2-1ubuntu1) ...
Setting up fonts-dejavu-extra (2.37-8) ...
Setting up alsa-topology-conf (1.2.5.1-2) ...
Setting up libxshmfence1:amd64 (1.3-1build5) ...
Setting up at-spi2-common (2.52.0-1build1) ...
Setting up libxcb-randr0:amd64 (1.15-1ubuntu2) ...
Setting up libharfbuzz0b:amd64 (8.3.0-2build2) ...
Setting up libthai-data (0.1.29-2build1) ...
Setting up xorg-sgml-doctools (1:1.11-1.1) ...
Setting up libgdk-pixbuf-2.0-0:amd64 (2.42.10+dfsg-3ubuntu3.3) ...
Setting up libcairo-gobject2:amd64 (1.18.0-3build1) ...
Setting up libgtk2.0-common (2.24.33-4ubuntu1.1) ...
Setting up libwagon-http-shaded-java (3.5.3-1) ...
Setting up libxkbfile1:amd64 (1:1.1.0-1build4) ...
Setting up ca-certificates-java (20240118) ...
No JRE found. Skipping Java certificates setup.
Setting up libcdi-api-java (1.2-3) ...
Setting up libxcomposite1:amd64 (1:0.4.5-1build3) ...
Setting up libwayland-client0:amd64 (1.22.0-2.1build1) ...
Setting up libwagon-provider-api-java (3.5.3-1) ...
Setting up mesa-vulkan-drivers:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Setting up gtk-update-icon-cache (3.24.41-4ubuntu1.3) ...
Setting up libice6:amd64 (2:1.0.10-1build3) ...
Setting up mesa-libgallium:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Setting up libxft2:amd64 (2.3.6-1build1) ...
Setting up libatk1.0-0t64:amd64 (2.52.0-1build1) ...
Setting up libgbm1:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Setting up alsa-ucm-conf (1.2.10-1ubuntu5.11) ...
Setting up libxtst6:amd64 (2:1.2.3-1.1build1) ...
Setting up libxcursor1:amd64 (1:1.2.1-1build1) ...
Setting up libmaven-parent-java (35-1) ...
Setting up libgl1-mesa-dri:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Setting up libcommons-parent-java (56-1) ...
Setting up libavahi-common3:amd64 (0.8-13ubuntu6.2) ...
Setting up libsisu-inject-java (0.3.4-2) ...
Setting up dconf-service (0.40.0-4ubuntu0.1) ...
Setting up libplexus-cipher-java (2.0-1) ...
Setting up libthai0:amd64 (0.1.29-2build1) ...
Setting up libsisu-plexus-java (0.3.4-3) ...
Setting up libcommons-lang3-java (3.14.0-1) ...
Setting up openjdk-17-jre-headless:amd64 (17.0.19+10-1~24.04.2) ...
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/java to provide /usr/bin/java (java) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jpackage to provide /usr/bin/jpackage (jpackage) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/keytool to provide /usr/bin/keytool (keytool) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/rmiregistry to provide /usr/bin/rmiregistry (rmiregistry) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/lib/jexec to provide /usr/bin/jexec (jexec) in auto mode
Setting up libgdk-pixbuf2.0-bin (2.42.10+dfsg-3ubuntu3.3) ...
Setting up libplexus-sec-dispatcher-java (2.0-3) ...
Setting up libwagon-file-java (3.5.3-1) ...
Setting up libsm6:amd64 (2:1.2.3-1build3) ...
Setting up libavahi-client3:amd64 (0.8-13ubuntu6.2) ...
Setting up libatk-bridge2.0-0t64:amd64 (2.52.0-1build1) ...
Setting up libglx-mesa0:amd64 (25.2.8-0ubuntu0.24.04.1) ...
Setting up libglx0:amd64 (1.7.0-1build1) ...
Setting up dconf-gsettings-backend:amd64 (0.40.0-4ubuntu0.1) ...
Setting up libcommons-io-java (2.11.0-2) ...
Setting up libpango-1.0-0:amd64 (1.52.1+ds-1build1) ...
Setting up libmaven-resolver-java (1.6.3-1) ...
Setting up libgl1:amd64 (1.7.0-1build1) ...
Setting up libxt6t64:amd64 (1:1.2.1-1.2build1) ...
Setting up libmaven-shared-utils-java (3.3.4-1) ...
Setting up libpangoft2-1.0-0:amd64 (1.52.1+ds-1build1) ...
Setting up libcups2t64:amd64 (2.4.7-1.2ubuntu7.13) ...
Setting up libpangocairo-1.0-0:amd64 (1.52.1+ds-1build1) ...
Setting up gsettings-desktop-schemas (46.1-0ubuntu1) ...
Setting up libxmu6:amd64 (2:1.1.3-3build2) ...
Setting up libxaw7:amd64 (2:1.0.14-1build2) ...
Setting up librsvg2-2:amd64 (2.58.0+dfsg-1build1) ...
Setting up librsvg2-common:amd64 (2.58.0+dfsg-1build1) ...
Setting up x11-utils (7.7+6build2) ...
Setting up libatk-wrapper-java (0.40.0-3build2) ...
Setting up libatk-wrapper-java-jni:amd64 (0.40.0-3build2) ...
Setting up adwaita-icon-theme (46.0-1) ...
update-alternatives: using /usr/share/icons/Adwaita/cursor.theme to provide /usr/share/icons/default/index.theme (x-cursor-theme) in auto mode
Setting up libgtk2.0-0t64:amd64 (2.24.33-4ubuntu1.1) ...
Setting up libgail18t64:amd64 (2.24.33-4ubuntu1.1) ...
Setting up libgtk2.0-bin (2.24.33-4ubuntu1.1) ...
Setting up liberror-prone-java (2.18.0-1) ...
Setting up libgail-common:amd64 (2.24.33-4ubuntu1.1) ...
Setting up humanity-icon-theme (0.6.16) ...
Setting up libguava-java (32.0.1-1) ...
Setting up ubuntu-mono (24.04-0ubuntu1) ...
Setting up libguice-java (4.2.3-2) ...
Setting up libmaven3-core-java (3.8.7-2) ...
Processing triggers for libc-bin (2.39-0ubuntu8.7) ...
Processing triggers for man-db (2.12.0-4build2) ...
Processing triggers for libglib2.0-0t64:amd64 (2.80.0-6ubuntu3.8) ...
Setting up at-spi2-core (2.52.0-1build1) ...
Processing triggers for sgml-base (1.31) ...
Setting up x11proto-dev (2023.2-1) ...
Setting up libxau-dev:amd64 (1:1.0.9-1build6) ...
Setting up libice-dev:amd64 (2:1.0.10-1build3) ...
Setting up libsm-dev:amd64 (2:1.2.3-1build3) ...
Setting up libxdmcp-dev:amd64 (1:1.1.3-0ubuntu6) ...
Setting up libxcb1-dev:amd64 (1.15-1ubuntu2) ...
Setting up libx11-dev:amd64 (2:1.8.7-1build1) ...
Setting up libxt-dev:amd64 (1:1.2.1-1.2build1) ...
Processing triggers for ca-certificates-java (20240118) ...
Adding debian:ACCVRAIZ1.pem
Adding debian:AC_RAIZ_FNMT-RCM.pem
Adding debian:AC_RAIZ_FNMT-RCM_SERVIDORES_SEGUROS.pem
Adding debian:ANF_Secure_Server_Root_CA.pem
Adding debian:Actalis_Authentication_Root_CA.pem
Adding debian:AffirmTrust_Commercial.pem
Adding debian:AffirmTrust_Networking.pem
Adding debian:AffirmTrust_Premium.pem
Adding debian:AffirmTrust_Premium_ECC.pem
Adding debian:Amazon_Root_CA_1.pem
Adding debian:Amazon_Root_CA_2.pem
Adding debian:Amazon_Root_CA_3.pem
Adding debian:Amazon_Root_CA_4.pem
Adding debian:Atos_TrustedRoot_2011.pem
Adding debian:Atos_TrustedRoot_Root_CA_ECC_TLS_2021.pem
Adding debian:Atos_TrustedRoot_Root_CA_RSA_TLS_2021.pem
Adding debian:Autoridad_de_Certificacion_Firmaprofesional_CIF_A62634068.pem
Adding debian:BJCA_Global_Root_CA1.pem
Adding debian:BJCA_Global_Root_CA2.pem
Adding debian:Baltimore_CyberTrust_Root.pem
Adding debian:Buypass_Class_2_Root_CA.pem
Adding debian:Buypass_Class_3_Root_CA.pem
Adding debian:CA_Disig_Root_R2.pem
Adding debian:CFCA_EV_ROOT.pem
Adding debian:COMODO_Certification_Authority.pem
Adding debian:COMODO_ECC_Certification_Authority.pem
Adding debian:COMODO_RSA_Certification_Authority.pem
Adding debian:Certainly_Root_E1.pem
Adding debian:Certainly_Root_R1.pem
Adding debian:Certigna.pem
Adding debian:Certigna_Root_CA.pem
Adding debian:Certum_EC-384_CA.pem
Adding debian:Certum_Trusted_Network_CA.pem
Adding debian:Certum_Trusted_Network_CA_2.pem
Adding debian:Certum_Trusted_Root_CA.pem
Adding debian:CommScope_Public_Trust_ECC_Root-01.pem
Adding debian:CommScope_Public_Trust_ECC_Root-02.pem
Adding debian:CommScope_Public_Trust_RSA_Root-01.pem
Adding debian:CommScope_Public_Trust_RSA_Root-02.pem
Adding debian:Comodo_AAA_Services_root.pem
Adding debian:D-TRUST_BR_Root_CA_1_2020.pem
Adding debian:D-TRUST_EV_Root_CA_1_2020.pem
Adding debian:D-TRUST_Root_Class_3_CA_2_2009.pem
Adding debian:D-TRUST_Root_Class_3_CA_2_EV_2009.pem
Adding debian:DigiCert_Assured_ID_Root_CA.pem
Adding debian:DigiCert_Assured_ID_Root_G2.pem
Adding debian:DigiCert_Assured_ID_Root_G3.pem
Adding debian:DigiCert_Global_Root_CA.pem
Adding debian:DigiCert_Global_Root_G2.pem
Adding debian:DigiCert_Global_Root_G3.pem
Adding debian:DigiCert_High_Assurance_EV_Root_CA.pem
Adding debian:DigiCert_TLS_ECC_P384_Root_G5.pem
Adding debian:DigiCert_TLS_RSA4096_Root_G5.pem
Adding debian:DigiCert_Trusted_Root_G4.pem
Adding debian:Entrust.net_Premium_2048_Secure_Server_CA.pem
Adding debian:Entrust_Root_Certification_Authority.pem
Adding debian:Entrust_Root_Certification_Authority_-_EC1.pem
Adding debian:Entrust_Root_Certification_Authority_-_G2.pem
Adding debian:Entrust_Root_Certification_Authority_-_G4.pem
Adding debian:GDCA_TrustAUTH_R5_ROOT.pem
Adding debian:GLOBALTRUST_2020.pem
Adding debian:GTS_Root_R1.pem
Adding debian:GTS_Root_R2.pem
Adding debian:GTS_Root_R3.pem
Adding debian:GTS_Root_R4.pem
Adding debian:GlobalSign_ECC_Root_CA_-_R4.pem
Adding debian:GlobalSign_ECC_Root_CA_-_R5.pem
Adding debian:GlobalSign_Root_CA.pem
Adding debian:GlobalSign_Root_CA_-_R3.pem
Adding debian:GlobalSign_Root_CA_-_R6.pem
Adding debian:GlobalSign_Root_E46.pem
Adding debian:GlobalSign_Root_R46.pem
Adding debian:Go_Daddy_Class_2_CA.pem
Adding debian:Go_Daddy_Root_Certificate_Authority_-_G2.pem
Adding debian:HARICA_TLS_ECC_Root_CA_2021.pem
Adding debian:HARICA_TLS_RSA_Root_CA_2021.pem
Adding debian:Hellenic_Academic_and_Research_Institutions_ECC_RootCA_2015.pem
Adding debian:Hellenic_Academic_and_Research_Institutions_RootCA_2015.pem
Adding debian:HiPKI_Root_CA_-_G1.pem
Adding debian:Hongkong_Post_Root_CA_3.pem
Adding debian:ISRG_Root_X1.pem
Adding debian:ISRG_Root_X2.pem
Adding debian:IdenTrust_Commercial_Root_CA_1.pem
Adding debian:IdenTrust_Public_Sector_Root_CA_1.pem
Adding debian:Izenpe.com.pem
Adding debian:Microsec_e-Szigno_Root_CA_2009.pem
Adding debian:Microsoft_ECC_Root_Certificate_Authority_2017.pem
Adding debian:Microsoft_RSA_Root_Certificate_Authority_2017.pem
Adding debian:NAVER_Global_Root_Certification_Authority.pem
Adding debian:NetLock_Arany_=Class_Gold=_Főtanúsítvány.pem
Adding debian:OISTE_WISeKey_Global_Root_GB_CA.pem
Adding debian:OISTE_WISeKey_Global_Root_GC_CA.pem
Adding debian:QuoVadis_Root_CA_1_G3.pem
Adding debian:QuoVadis_Root_CA_2.pem
Adding debian:QuoVadis_Root_CA_2_G3.pem
Adding debian:QuoVadis_Root_CA_3.pem
Adding debian:QuoVadis_Root_CA_3_G3.pem
Adding debian:SSL.com_EV_Root_Certification_Authority_ECC.pem
Adding debian:SSL.com_EV_Root_Certification_Authority_RSA_R2.pem
Adding debian:SSL.com_Root_Certification_Authority_ECC.pem
Adding debian:SSL.com_Root_Certification_Authority_RSA.pem
Adding debian:SSL.com_TLS_ECC_Root_CA_2022.pem
Adding debian:SSL.com_TLS_RSA_Root_CA_2022.pem
Adding debian:SZAFIR_ROOT_CA2.pem
Adding debian:Sectigo_Public_Server_Authentication_Root_E46.pem
Adding debian:Sectigo_Public_Server_Authentication_Root_R46.pem
Adding debian:SecureSign_RootCA11.pem
Adding debian:SecureTrust_CA.pem
Adding debian:Secure_Global_CA.pem
Adding debian:Security_Communication_ECC_RootCA1.pem
Adding debian:Security_Communication_RootCA2.pem
Adding debian:Security_Communication_RootCA3.pem
Adding debian:Security_Communication_Root_CA.pem
Adding debian:Starfield_Class_2_CA.pem
Adding debian:Starfield_Root_Certificate_Authority_-_G2.pem
Adding debian:Starfield_Services_Root_Certificate_Authority_-_G2.pem
Adding debian:SwissSign_Gold_CA_-_G2.pem
Adding debian:SwissSign_Silver_CA_-_G2.pem
Adding debian:T-TeleSec_GlobalRoot_Class_2.pem
Adding debian:T-TeleSec_GlobalRoot_Class_3.pem
Adding debian:TUBITAK_Kamu_SM_SSL_Kok_Sertifikasi_-_Surum_1.pem
Adding debian:TWCA_Global_Root_CA.pem
Adding debian:TWCA_Root_Certification_Authority.pem
Adding debian:TeliaSonera_Root_CA_v1.pem
Adding debian:Telia_Root_CA_v2.pem
Adding debian:TrustAsia_Global_Root_CA_G3.pem
Adding debian:TrustAsia_Global_Root_CA_G4.pem
Adding debian:Trustwave_Global_Certification_Authority.pem
Adding debian:Trustwave_Global_ECC_P256_Certification_Authority.pem
Adding debian:Trustwave_Global_ECC_P384_Certification_Authority.pem
Adding debian:TunTrust_Root_CA.pem
Adding debian:UCA_Extended_Validation_Root.pem
Adding debian:UCA_Global_G2_Root.pem
Adding debian:USERTrust_ECC_Certification_Authority.pem
Adding debian:USERTrust_RSA_Certification_Authority.pem
Adding debian:XRamp_Global_CA_Root.pem
Adding debian:certSIGN_ROOT_CA.pem
Adding debian:certSIGN_Root_CA_G2.pem
Adding debian:e-Szigno_Root_CA_2017.pem
Adding debian:ePKI_Root_Certification_Authority.pem
Adding debian:emSign_ECC_Root_CA_-_C3.pem
Adding debian:emSign_ECC_Root_CA_-_G3.pem
Adding debian:emSign_Root_CA_-_C1.pem
Adding debian:emSign_Root_CA_-_G1.pem
Adding debian:vTrus_ECC_Root_CA.pem
Adding debian:vTrus_Root_CA.pem
done.
Setting up openjdk-17-jre:amd64 (17.0.19+10-1~24.04.2) ...
Setting up maven (3.8.7-2) ...
update-alternatives: using /usr/share/maven/bin/mvn to provide /usr/bin/mvn (mvn) in auto mode
Setting up openjdk-17-jdk-headless:amd64 (17.0.19+10-1~24.04.2) ...
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jar to provide /usr/bin/jar (jar) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jarsigner to provide /usr/bin/jarsigner (jarsigner) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/javac to provide /usr/bin/javac (javac) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/javadoc to provide /usr/bin/javadoc (javadoc) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/javap to provide /usr/bin/javap (javap) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jcmd to provide /usr/bin/jcmd (jcmd) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jdb to provide /usr/bin/jdb (jdb) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jdeprscan to provide /usr/bin/jdeprscan (jdeprscan) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jdeps to provide /usr/bin/jdeps (jdeps) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jfr to provide /usr/bin/jfr (jfr) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jimage to provide /usr/bin/jimage (jimage) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jinfo to provide /usr/bin/jinfo (jinfo) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jlink to provide /usr/bin/jlink (jlink) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jmap to provide /usr/bin/jmap (jmap) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jmod to provide /usr/bin/jmod (jmod) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jps to provide /usr/bin/jps (jps) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jrunscript to provide /usr/bin/jrunscript (jrunscript) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jshell to provide /usr/bin/jshell (jshell) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jstack to provide /usr/bin/jstack (jstack) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jstat to provide /usr/bin/jstat (jstat) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jstatd to provide /usr/bin/jstatd (jstatd) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/serialver to provide /usr/bin/serialver (serialver) in auto mode
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jhsdb to provide /usr/bin/jhsdb (jhsdb) in auto mode
Setting up openjdk-17-jdk:amd64 (17.0.19+10-1~24.04.2) ...
update-alternatives: using /usr/lib/jvm/java-17-openjdk-amd64/bin/jconsole to provide /usr/bin/jconsole (jconsole) in auto mode
Processing triggers for libgdk-pixbuf-2.0-0:amd64 (2.42.10+dfsg-3ubuntu3.3) ...

Running kernel seems to be up-to-date.

No services need to be restarted.

No containers need to be restarted.

No user sessions are running outdated binaries.

No VM guests are running outdated hypervisor (qemu) binaries on this host.

[ENV_SETUP_EXIT_CODE=0]
```

## 测试执行日志（重试：安装 JDK/Maven 后）

命令：`mvn -f java-ai-assistant/pom.xml verify`

```text
[[1;34mINFO[m] Scanning for projects...
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--------------------< [0;36massistant:java-ai-assistant[0;1m >---------------------[m
[[1;34mINFO[m] [1mBuilding java-ai-assistant 1.0.0-SNAPSHOT[m
[[1;34mINFO[m] [1m--------------------------------[ jar ]---------------------------------[m
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/jacoco-maven-plugin/0.8.13/jacoco-maven-plugin-0.8.13.pom
Progress (1): 4.1 kBProgress (1): 4.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/jacoco-maven-plugin/0.8.13/jacoco-maven-plugin-0.8.13.pom (4.2 kB at 6.4 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.build/0.8.13/org.jacoco.build-0.8.13.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 46 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.build/0.8.13/org.jacoco.build-0.8.13.pom (46 kB at 492 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-bom/9.8/asm-bom-9.8.pom
Progress (1): 3.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-bom/9.8/asm-bom-9.8.pom (3.3 kB at 60 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/ow2/1.5.1/ow2-1.5.1.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 11 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/ow2/1.5.1/ow2-1.5.1.pom (11 kB at 179 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/jacoco-maven-plugin/0.8.13/jacoco-maven-plugin-0.8.13.jar
Progress (1): 2.3/57 kBProgress (1): 5.0/57 kBProgress (1): 7.7/57 kBProgress (1): 10/57 kB Progress (1): 13/57 kBProgress (1): 16/57 kBProgress (1): 19/57 kBProgress (1): 21/57 kBProgress (1): 24/57 kBProgress (1): 27/57 kBProgress (1): 31/57 kBProgress (1): 36/57 kBProgress (1): 40/57 kBProgress (1): 44/57 kBProgress (1): 48/57 kBProgress (1): 52/57 kBProgress (1): 56/57 kBProgress (1): 57 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/jacoco-maven-plugin/0.8.13/jacoco-maven-plugin-0.8.13.jar (57 kB at 532 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/2.6/maven-resources-plugin-2.6.pom
Progress (1): 4.1 kBProgress (1): 8.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/2.6/maven-resources-plugin-2.6.pom (8.1 kB at 129 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/23/maven-plugins-23.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/23/maven-plugins-23.pom (9.2 kB at 156 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/22/maven-parent-22.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 30 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/22/maven-parent-22.pom (30 kB at 431 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/11/apache-11.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 15 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/11/apache-11.pom (15 kB at 269 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/2.6/maven-resources-plugin-2.6.jar
Progress (1): 3.8/30 kBProgress (1): 7.8/30 kBProgress (1): 11/30 kB Progress (1): 15/30 kBProgress (1): 20/30 kBProgress (1): 24/30 kBProgress (1): 28/30 kBProgress (1): 30 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-resources-plugin/2.6/maven-resources-plugin-2.6.jar (30 kB at 518 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.14.0/maven-compiler-plugin-3.14.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.14.0/maven-compiler-plugin-3.14.0.pom (9.5 kB at 131 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/43/maven-plugins-43.pom
Progress (1): 4.1 kBProgress (1): 7.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/43/maven-plugins-43.pom (7.5 kB at 108 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/43/maven-parent-43.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 49 kBProgress (1): 50 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/43/maven-parent-43.pom (50 kB at 662 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/33/apache-33.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 24 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/33/apache-33.pom (24 kB at 456 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.10.3/junit-bom-5.10.3.pom
Progress (1): 4.1 kBProgress (1): 5.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.10.3/junit-bom-5.10.3.pom (5.6 kB at 105 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.14.0/maven-compiler-plugin-3.14.0.jar
Progress (1): 4.1/83 kBProgress (1): 7.7/83 kBProgress (1): 12/83 kB Progress (1): 16/83 kBProgress (1): 20/83 kBProgress (1): 24/83 kBProgress (1): 28/83 kBProgress (1): 32/83 kBProgress (1): 36/83 kBProgress (1): 40/83 kBProgress (1): 45/83 kBProgress (1): 49/83 kBProgress (1): 53/83 kBProgress (1): 57/83 kBProgress (1): 61/83 kBProgress (1): 65/83 kBProgress (1): 69/83 kBProgress (1): 73/83 kBProgress (1): 77/83 kBProgress (1): 81/83 kBProgress (1): 83 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.14.0/maven-compiler-plugin-3.14.0.jar (83 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-surefire-plugin/3.5.6/maven-surefire-plugin-3.5.6.pom
Progress (1): 4.1 kBProgress (1): 4.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-surefire-plugin/3.5.6/maven-surefire-plugin-3.5.6.pom (4.9 kB at 87 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire/3.5.6/surefire-3.5.6.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 19 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire/3.5.6/surefire-3.5.6.pom (19 kB at 356 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/47/maven-parent-47.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 49 kBProgress (1): 53 kBProgress (1): 54 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/47/maven-parent-47.pom (54 kB at 924 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/37/apache-37.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 26 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/37/apache-37.pom (26 kB at 427 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.12.2/junit-bom-5.12.2.pom
Progress (1): 4.1 kBProgress (1): 5.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.12.2/junit-bom-5.12.2.pom (5.6 kB at 115 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-surefire-plugin/3.5.6/maven-surefire-plugin-3.5.6.jar
Progress (1): 4.1/47 kBProgress (1): 7.7/47 kBProgress (1): 11/47 kB Progress (1): 15/47 kBProgress (1): 20/47 kBProgress (1): 24/47 kBProgress (1): 28/47 kBProgress (1): 32/47 kBProgress (1): 36/47 kBProgress (1): 40/47 kBProgress (1): 44/47 kBProgress (1): 47 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-surefire-plugin/3.5.6/maven-surefire-plugin-3.5.6.jar (47 kB at 668 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-jar-plugin/2.4/maven-jar-plugin-2.4.pom
Progress (1): 4.1 kBProgress (1): 5.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-jar-plugin/2.4/maven-jar-plugin-2.4.pom (5.8 kB at 93 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/22/maven-plugins-22.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 13 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-plugins/22/maven-plugins-22.pom (13 kB at 251 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/21/maven-parent-21.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 26 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/21/maven-parent-21.pom (26 kB at 377 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/10/apache-10.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 15 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/10/apache-10.pom (15 kB at 279 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-jar-plugin/2.4/maven-jar-plugin-2.4.jar
Progress (1): 4.1/34 kBProgress (1): 7.7/34 kBProgress (1): 12/34 kB Progress (1): 16/34 kBProgress (1): 20/34 kBProgress (1): 24/34 kBProgress (1): 28/34 kBProgress (1): 32/34 kBProgress (1): 34 kB                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-jar-plugin/2.4/maven-jar-plugin-2.4.jar (34 kB at 549 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.19.0/jackson-databind-2.19.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 23 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.19.0/jackson-databind-2.19.0.pom (23 kB at 389 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-base/2.19.0/jackson-base-2.19.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 13 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-base/2.19.0/jackson-base-2.19.0.pom (13 kB at 133 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-bom/2.19.0/jackson-bom-2.19.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-bom/2.19.0/jackson-bom-2.19.0.pom (20 kB at 365 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-parent/2.19/jackson-parent-2.19.pom
Progress (1): 4.1 kBProgress (1): 6.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/jackson-parent/2.19/jackson-parent-2.19.pom (6.7 kB at 119 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/oss-parent/65/oss-parent-65.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 23 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/oss-parent/65/oss-parent-65.pom (23 kB at 396 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.11.4/junit-bom-5.11.4.pom
Progress (1): 4.1 kBProgress (1): 5.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.11.4/junit-bom-5.11.4.pom (5.6 kB at 128 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.19.0/jackson-annotations-2.19.0.pom
Progress (1): 4.1 kBProgress (1): 7.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.19.0/jackson-annotations-2.19.0.pom (7.2 kB at 133 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.19.0/jackson-core-2.19.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.19.0/jackson-core-2.19.0.pom (9.5 kB at 183 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/5.14.4/junit-jupiter-5.14.4.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/5.14.4/junit-jupiter-5.14.4.pom (3.2 kB at 60 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.14.4/junit-bom-5.14.4.pom
Progress (1): 4.1 kBProgress (1): 5.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.14.4/junit-bom-5.14.4.pom (5.7 kB at 109 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.14.4/junit-jupiter-api-5.14.4.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.14.4/junit-jupiter-api-5.14.4.pom (3.2 kB at 64 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.pom
Progress (1): 2.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.pom (2.0 kB at 36 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.14.4/junit-platform-commons-1.14.4.pom
Progress (1): 2.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.14.4/junit-platform-commons-1.14.4.pom (2.9 kB at 55 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.pom
Progress (1): 1.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.pom (1.5 kB at 29 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-params/5.14.4/junit-jupiter-params-5.14.4.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-params/5.14.4/junit-jupiter-params-5.14.4.pom (3.0 kB at 56 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.14.4/junit-jupiter-engine-5.14.4.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.14.4/junit-jupiter-engine-5.14.4.pom (3.2 kB at 66 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.14.4/junit-platform-engine-1.14.4.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.14.4/junit-platform-engine-1.14.4.pom (3.2 kB at 67 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-core/5.18.0/mockito-core-5.18.0.pom
Progress (1): 2.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-core/5.18.0/mockito-core-5.18.0.pom (2.5 kB at 51 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.17.5/byte-buddy-1.17.5.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 19 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.17.5/byte-buddy-1.17.5.pom (19 kB at 411 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-parent/1.17.5/byte-buddy-parent-1.17.5.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 49 kBProgress (1): 53 kBProgress (1): 57 kBProgress (1): 61 kBProgress (1): 65 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-parent/1.17.5/byte-buddy-parent-1.17.5.pom (65 kB at 923 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.17.5/byte-buddy-agent-1.17.5.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.17.5/byte-buddy-agent-1.17.5.pom (12 kB at 242 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis/3.3/objenesis-3.3.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis/3.3/objenesis-3.3.pom (3.0 kB at 44 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis-parent/3.3/objenesis-parent-3.3.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 19 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis-parent/3.3/objenesis-parent-3.3.pom (19 kB at 319 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-junit-jupiter/5.18.0/mockito-junit-jupiter-5.18.0.pom
Progress (1): 2.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-junit-jupiter/5.18.0/mockito-junit-jupiter-5.18.0.pom (2.3 kB at 40 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.11.4/junit-jupiter-api-5.11.4.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.11.4/junit-jupiter-api-5.11.4.pom (3.2 kB at 66 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.11.4/junit-platform-commons-1.11.4.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.11.4/junit-platform-commons-1.11.4.pom (2.8 kB at 59 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.19.0/jackson-databind-2.19.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.19.0/jackson-annotations-2.19.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.19.0/jackson-core-2.19.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/5.14.4/junit-jupiter-5.14.4.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.14.4/junit-jupiter-api-5.14.4.jar
Progress (1): 0/1.7 MBProgress (1): 0/1.7 MBProgress (1): 0/1.7 MBProgress (1): 0/1.7 MBProgress (1): 0/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.1/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (1): 0.2/1.7 MBProgress (2): 0.2/1.7 MB | 2.3/79 kBProgress (2): 0.2/1.7 MB | 5.0/79 kBProgress (2): 0.2/1.7 MB | 5.0/79 kBProgress (2): 0.2/1.7 MB | 5.0/79 kBProgress (3): 0.2/1.7 MB | 5.0/79 kB | 2.3/600 kBProgress (3): 0.2/1.7 MB | 7.7/79 kB | 2.3/600 kBProgress (3): 0.2/1.7 MB | 10/79 kB | 2.3/600 kB Progress (3): 0.2/1.7 MB | 13/79 kB | 2.3/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 2.3/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 2.3/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 5.0/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 7.7/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 10/600 kB Progress (3): 0.2/1.7 MB | 13/79 kB | 13/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 16/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.2/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 13/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 15/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 18/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 21/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 19/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 21/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 24/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 27/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 30/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 32/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 35/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 38/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 41/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 43/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 46/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 49/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 24/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 26/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 29/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 32/79 kB | 52/600 kBProgress (3): 0.3/1.7 MB | 35/79 kB | 52/600 kBProgress (4): 0.3/1.7 MB | 35/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 35/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 35/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 35/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 37/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 40/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 52/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 54/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 58/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 62/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 67/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 71/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 75/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 79/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 81/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 85/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 89/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 93/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 97/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 102/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 106/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 43/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 46/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 48/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 51/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 54/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 58/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 62/79 kB | 110/600 kB | 2.3/6.4 kBProgress (4): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 2.3/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 5.0/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 7.7/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 10/242 kB Progress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 13/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 16/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 19/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 21/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 24/242 kBProgress (5): 0.3/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 66/79 kB | 110/600 kB | 2.3/6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 66/79 kB | 110/600 kB | 5.5/6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 66/79 kB | 110/600 kB | 6.4 kB | 24/242 kB    Progress (5): 0.4/1.7 MB | 70/79 kB | 110/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 74/79 kB | 110/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 78/79 kB | 110/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 110/600 kB | 6.4 kB | 24/242 kB   Progress (5): 0.4/1.7 MB | 79 kB | 114/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 118/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 24/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 27/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 30/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 32/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 122/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 126/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 130/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 134/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 134/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 134/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 134/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 134/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 138/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 142/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 147/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 35/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 38/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 41/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 43/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 46/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 46/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 46/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 49/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 52/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 151/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 155/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 159/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 163/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.4/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.5/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.5/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.5/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kBProgress (5): 0.5/1.7 MB | 79 kB | 167/600 kB | 6.4 kB | 54/242 kB                                                                  Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter/5.14.4/junit-jupiter-5.14.4.jar (6.4 kB at 32 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar
Progress (4): 0.5/1.7 MB | 79 kB | 171/600 kB | 54/242 kBProgress (4): 0.5/1.7 MB | 79 kB | 171/600 kB | 58/242 kBProgress (4): 0.5/1.7 MB | 79 kB | 171/600 kB | 62/242 kBProgress (4): 0.5/1.7 MB | 79 kB | 171/600 kB | 65/242 kBProgress (4): 0.5/1.7 MB | 79 kB | 171/600 kB | 69/242 kB                                                         Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-annotations/2.19.0/jackson-annotations-2.19.0.jar (79 kB at 387 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.14.4/junit-platform-commons-1.14.4.jar
Progress (3): 0.5/1.7 MB | 175/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 177/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 69/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 73/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 77/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 81/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 85/242 kBProgress (3): 0.5/1.7 MB | 181/600 kB | 89/242 kBProgress (3): 0.5/1.7 MB | 186/600 kB | 89/242 kBProgress (3): 0.5/1.7 MB | 190/600 kB | 89/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 89/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 93/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 97/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 101/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 194/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 198/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 202/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 206/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 106/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 110/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 114/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 118/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 210/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 214/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 218/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 222/600 kB | 122/242 kBProgress (3): 0.5/1.7 MB | 222/600 kB | 126/242 kBProgress (3): 0.5/1.7 MB | 222/600 kB | 130/242 kBProgress (3): 0.5/1.7 MB | 222/600 kB | 132/242 kBProgress (4): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 3.8/14 kBProgress (4): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 7.8/14 kBProgress (4): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 11/14 kB Progress (4): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB   Progress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 2.3/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 5.0/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 7.7/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 11/164 kB Progress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 14/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 16/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 19/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 132/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 136/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 140/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 222/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 227/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 231/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 235/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 239/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 243/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.5/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 22/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 25/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 27/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 30/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 33/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 36/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 38/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.6/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 247/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 251/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 253/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 258/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 262/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 266/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 270/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 274/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 278/600 kB | 145/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 278/600 kB | 149/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 278/600 kB | 153/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 278/600 kB | 157/242 kB | 14 kB | 41/164 kBProgress (5): 0.7/1.7 MB | 278/600 kB | 161/242 kB | 14 kB | 41/164 kB                                                                      Downloaded from central: https://repo.maven.apache.org/maven2/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar (14 kB at 58 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar
Progress (4): 0.7/1.7 MB | 278/600 kB | 161/242 kB | 44/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 161/242 kB | 48/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 161/242 kB | 52/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 161/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 165/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 169/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 173/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 177/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 181/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 186/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 56/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 60/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 64/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 68/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 72/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 278/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 282/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 286/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 290/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 294/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 299/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 190/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 194/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 198/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 200/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 204/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 75/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 79/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 82/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 86/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 90/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 94/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 98/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 102/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 106/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 111/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 115/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 119/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 123/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 127/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 131/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 135/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 139/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 143/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 208/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 212/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 216/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 303/600 kB | 220/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 307/600 kB | 220/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 311/600 kB | 220/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 315/600 kB | 220/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 319/600 kB | 220/242 kB | 147/164 kBProgress (4): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 147/164 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 147/164 kB | 3.8/6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 147/164 kB | 6.8 kB    Progress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 152/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 156/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 160/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 220/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 224/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 229/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 233/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 237/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.7/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164/164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 323/600 kB | 241/242 kB | 164 kB | 6.8 kB    Progress (5): 0.8/1.7 MB | 327/600 kB | 241/242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 331/600 kB | 241/242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 331/600 kB | 242 kB | 164 kB | 6.8 kB    Progress (5): 0.8/1.7 MB | 335/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 340/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 344/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 348/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 352/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 356/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 360/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 364/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 368/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 372/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 376/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 380/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 385/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 389/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 389/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 389/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 389/600 kB | 242 kB | 164 kB | 6.8 kBProgress (5): 0.8/1.7 MB | 389/600 kB | 242 kB | 164 kB | 6.8 kB                                                                Downloaded from central: https://repo.maven.apache.org/maven2/org/apiguardian/apiguardian-api/1.1.2/apiguardian-api-1.1.2.jar (6.8 kB at 21 kB/s)
Progress (4): 0.8/1.7 MB | 393/600 kB | 242 kB | 164 kBProgress (4): 0.8/1.7 MB | 397/600 kB | 242 kB | 164 kBProgress (4): 0.8/1.7 MB | 401/600 kB | 242 kB | 164 kB                                                       Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.14.4/junit-platform-commons-1.14.4.jar (164 kB at 517 kB/s)
Progress (3): 0.8/1.7 MB | 405/600 kB | 242 kBProgress (3): 0.8/1.7 MB | 409/600 kB | 242 kBProgress (3): 0.8/1.7 MB | 413/600 kB | 242 kBProgress (3): 0.8/1.7 MB | 417/600 kB | 242 kB                                              Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-params/5.14.4/junit-jupiter-params-5.14.4.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-api/5.14.4/junit-jupiter-api-5.14.4.jar (242 kB at 746 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.14.4/junit-jupiter-engine-5.14.4.jar
Progress (2): 0.8/1.7 MB | 417/600 kBProgress (2): 0.9/1.7 MB | 417/600 kBProgress (2): 0.9/1.7 MB | 417/600 kBProgress (2): 0.9/1.7 MB | 417/600 kB                                     Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.14.4/junit-platform-engine-1.14.4.jar
Progress (2): 0.9/1.7 MB | 421/600 kBProgress (2): 0.9/1.7 MB | 426/600 kBProgress (2): 0.9/1.7 MB | 430/600 kBProgress (2): 0.9/1.7 MB | 434/600 kBProgress (2): 0.9/1.7 MB | 438/600 kBProgress (2): 0.9/1.7 MB | 442/600 kBProgress (2): 0.9/1.7 MB | 446/600 kBProgress (2): 0.9/1.7 MB | 450/600 kBProgress (2): 0.9/1.7 MB | 450/600 kBProgress (2): 0.9/1.7 MB | 450/600 kBProgress (2): 0.9/1.7 MB | 454/600 kBProgress (2): 0.9/1.7 MB | 458/600 kBProgress (2): 0.9/1.7 MB | 462/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 466/600 kBProgress (2): 0.9/1.7 MB | 471/600 kBProgress (2): 0.9/1.7 MB | 475/600 kBProgress (2): 0.9/1.7 MB | 479/600 kBProgress (2): 0.9/1.7 MB | 483/600 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 3.2/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 7.3/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 11/663 kB Progress (3): 0.9/1.7 MB | 483/600 kB | 15/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 20/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 24/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 28/663 kBProgress (3): 0.9/1.7 MB | 483/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 487/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 491/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 495/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 499/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 503/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 507/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 512/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 512/600 kB | 32/663 kBProgress (3): 0.9/1.7 MB | 512/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 512/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 512/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 516/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 520/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 524/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 528/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 32/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 36/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 40/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 44/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 48/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 52/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 56/663 kBProgress (3): 1.0/1.7 MB | 532/600 kB | 61/663 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 61/663 kB | 3.2/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 61/663 kB | 7.3/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 61/663 kB | 11/344 kB Progress (4): 1.0/1.7 MB | 532/600 kB | 61/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 65/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 69/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 73/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 77/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 81/663 kB | 15/344 kBProgress (4): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 15/344 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 15/344 kB | 4.1/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 15/344 kB | 7.7/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 15/344 kB | 12/272 kB Progress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 15/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 20/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 24/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 28/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 32/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 532/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 536/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 540/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 544/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 548/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 552/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 85/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 89/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 93/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 97/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 101/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 106/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 15/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 20/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 24/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 28/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 32/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 36/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 40/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 44/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 36/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 40/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 44/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 48/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 52/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 56/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 110/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 114/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 118/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 122/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 126/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 130/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 134/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 138/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 142/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 557/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 561/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 565/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 569/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 573/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 577/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 48/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 52/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 56/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 61/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 65/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 69/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 61/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 65/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 69/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 71/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 75/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 79/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 83/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 73/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 77/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 81/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 85/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 89/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 93/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 97/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 97/272 kBProgress (5): 1.0/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 581/600 kB | 147/663 kB | 87/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 581/600 kB | 147/663 kB | 91/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 581/600 kB | 147/663 kB | 95/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 581/600 kB | 147/663 kB | 100/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 581/600 kB | 147/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 585/600 kB | 147/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 589/600 kB | 147/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 593/600 kB | 147/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 598/600 kB | 147/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 147/663 kB | 104/344 kB | 97/272 kB    Progress (5): 1.1/1.7 MB | 600 kB | 151/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 155/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 97/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 101/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 106/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 110/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 114/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 118/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 122/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 126/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 104/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 108/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 112/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 116/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 120/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 159/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 163/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 167/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 171/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 175/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 179/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 183/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 187/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 192/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 196/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 200/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 204/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 208/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 212/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 216/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 220/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 224/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 228/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 233/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 237/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 241/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 245/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 124/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 128/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 132/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 136/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 140/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 145/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 149/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 153/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 157/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 161/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 165/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 169/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 130/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 134/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 138/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 142/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 147/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 151/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 155/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 159/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 163/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 167/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 171/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 175/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 249/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 253/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 257/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 261/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 265/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 269/663 kB | 173/344 kB | 179/272 kBProgress (5): 1.1/1.7 MB | 600 kB | 274/663 kB | 173/344 kB | 179/272 kB                                                                        Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-core/2.19.0/jackson-core-2.19.0.jar (600 kB at 1.4 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-core/5.18.0/mockito-core-5.18.0.jar
Progress (4): 1.1/1.7 MB | 274/663 kB | 173/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 173/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 173/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 173/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 177/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 181/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 186/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 190/344 kB | 179/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 190/344 kB | 183/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 190/344 kB | 187/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 190/344 kB | 192/272 kBProgress (4): 1.1/1.7 MB | 274/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 278/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 282/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 190/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 194/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 198/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 202/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 286/663 kB | 206/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 206/344 kB | 196/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 206/344 kB | 200/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 206/344 kB | 204/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 206/344 kB | 208/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 206/344 kB | 212/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 210/344 kB | 212/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 214/344 kB | 212/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 218/344 kB | 212/272 kBProgress (4): 1.1/1.7 MB | 290/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 290/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 290/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 294/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 298/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 302/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 212/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 216/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 220/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 224/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 228/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 233/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 237/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 241/272 kBProgress (4): 1.2/1.7 MB | 306/663 kB | 222/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 310/663 kB | 222/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 314/663 kB | 222/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 319/663 kB | 222/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 222/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 226/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 231/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 235/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 239/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 243/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 247/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 251/344 kB | 245/272 kBProgress (4): 1.2/1.7 MB | 323/663 kB | 255/344 kB | 245/272 kBProgress (5): 1.2/1.7 MB | 323/663 kB | 255/344 kB | 245/272 kB | 4.1/710 kBProgress (5): 1.2/1.7 MB | 323/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 327/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 331/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 335/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 339/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 343/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 347/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 351/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 355/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 360/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 364/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 368/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 372/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 376/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 245/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 249/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 253/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 257/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 261/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 265/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 269/272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 7.7/710 kB    Progress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 7.7/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 255/344 kB | 272 kB | 11/710 kB Progress (5): 1.2/1.7 MB | 380/663 kB | 259/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 263/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 267/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 272/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 276/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 280/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 284/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 380/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 384/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 388/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 392/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 11/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 15/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 20/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 24/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 28/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 32/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 36/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 40/710 kBProgress (5): 1.2/1.7 MB | 396/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 400/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 405/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 409/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 413/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 417/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 44/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 48/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 52/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 56/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 61/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 65/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 69/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 73/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 73/710 kBProgress (5): 1.2/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 288/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 292/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 296/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 300/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 304/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 308/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 313/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 317/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 321/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 421/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 425/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 429/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 433/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 437/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 441/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 325/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 329/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 333/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 337/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 341/344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 446/663 kB | 344 kB | 272 kB | 73/710 kB    Progress (5): 1.3/1.7 MB | 450/663 kB | 344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 454/663 kB | 344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 458/663 kB | 344 kB | 272 kB | 73/710 kBProgress (5): 1.3/1.7 MB | 462/663 kB | 344 kB | 272 kB | 73/710 kB                                                                   Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.14.4/junit-platform-engine-1.14.4.jar (272 kB at 566 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.17.5/byte-buddy-1.17.5.jar
Progress (4): 1.3/1.7 MB | 466/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 470/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 474/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 73/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 77/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 81/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 85/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 89/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 478/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 482/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 487/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 491/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 495/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 499/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 503/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 507/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 511/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 515/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 519/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 93/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 97/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 101/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 106/710 kBProgress (4): 1.3/1.7 MB | 523/663 kB | 344 kB | 110/710 kB                                                           Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-engine/5.14.4/junit-jupiter-engine-5.14.4.jar (344 kB at 695 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.17.5/byte-buddy-agent-1.17.5.jar
Progress (3): 1.4/1.7 MB | 523/663 kB | 110/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 110/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 114/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 118/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 122/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 126/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 130/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 134/710 kBProgress (3): 1.4/1.7 MB | 523/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 527/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 532/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 536/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 540/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 138/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 142/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 147/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 151/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 155/710 kBProgress (3): 1.4/1.7 MB | 544/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 548/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 552/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 556/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 560/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 564/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 568/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 573/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 577/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 581/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 585/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 589/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 593/663 kB | 159/710 kBProgress (3): 1.4/1.7 MB | 593/663 kB | 163/710 kBProgress (3): 1.4/1.7 MB | 593/663 kB | 167/710 kBProgress (3): 1.4/1.7 MB | 593/663 kB | 171/710 kBProgress (3): 1.4/1.7 MB | 593/663 kB | 175/710 kBProgress (4): 1.4/1.7 MB | 593/663 kB | 175/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 175/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 175/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 175/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 175/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 179/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 183/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 187/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 192/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 196/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 200/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 204/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 208/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 212/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 216/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 220/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MBProgress (4): 1.4/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MBProgress (4): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MBProgress (4): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MBProgress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 4.1/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 7.7/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 12/366 kB Progress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 224/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 228/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 233/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 237/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 241/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 245/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 249/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 253/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 257/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 261/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 265/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 269/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 274/710 kB | 0/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 593/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 597/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 601/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 605/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 609/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 613/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 618/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 15/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 20/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 24/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 28/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 32/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 36/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 40/366 kBProgress (5): 1.5/1.7 MB | 622/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 626/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 630/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 634/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 638/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 274/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 278/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 282/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 286/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 290/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 294/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 298/710 kB | 0.1/9.0 MB | 44/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 298/710 kB | 0.1/9.0 MB | 48/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 298/710 kB | 0.1/9.0 MB | 52/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 642/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 646/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 650/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 654/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 659/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663/663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kB    Progress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.1/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 56/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 61/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 65/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 69/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 73/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 77/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 81/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 85/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 89/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 93/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 298/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 302/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 306/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 310/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.5/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 314/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 319/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 323/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 327/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 331/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 335/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 97/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 101/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 106/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 110/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 339/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 343/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 347/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 351/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.6/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 355/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 360/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 364/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 368/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 372/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 376/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.2/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 114/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 118/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 122/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 126/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 130/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 134/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 138/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 142/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 147/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 151/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 155/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 159/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 163/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 167/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 171/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 175/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 175/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 179/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 183/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 187/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 192/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 380/710 kB | 0.3/9.0 MB | 196/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 384/710 kB | 0.3/9.0 MB | 196/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 388/710 kB | 0.3/9.0 MB | 196/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 392/710 kB | 0.3/9.0 MB | 196/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 396/710 kB | 0.3/9.0 MB | 196/366 kBProgress (5): 1.7/1.7 MB | 663 kB | 396/710 kB | 0.4/9.0 MB | 196/366 kB                                                                        Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/jupiter/junit-jupiter-params/5.14.4/junit-jupiter-params-5.14.4.jar (663 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis/3.3/objenesis-3.3.jar
Progress (4): 1.7/1.7 MB | 400/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7/1.7 MB | 405/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7/1.7 MB | 409/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7/1.7 MB | 413/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7/1.7 MB | 417/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7/1.7 MB | 421/710 kB | 0.4/9.0 MB | 196/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 196/366 kB    Progress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 200/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 204/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 208/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 212/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 216/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 220/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 224/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 228/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 233/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 237/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 241/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 245/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 249/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 253/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 257/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 421/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 425/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 429/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 433/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 437/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 441/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 446/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 450/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 454/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 458/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 462/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 466/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 261/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 265/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 269/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 274/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 278/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 282/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 286/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 290/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 294/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 298/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 302/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 306/366 kBProgress (4): 1.7 MB | 470/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 474/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 478/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 482/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 487/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 491/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 495/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 499/710 kB | 0.4/9.0 MB | 310/366 kBProgress (4): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 4.1/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 7.7/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 12/49 kB Progress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 16/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 20/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 24/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 310/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 314/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 319/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 323/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 327/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 331/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 335/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 339/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 343/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 347/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 28/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 32/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 36/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 40/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 45/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 49/49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 351/366 kB | 49 kB   Progress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 355/366 kB | 49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 360/366 kB | 49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 364/366 kB | 49 kBProgress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 366 kB | 49 kB    Progress (5): 1.7 MB | 499/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 503/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 507/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 511/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 515/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 519/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (5): 1.7 MB | 523/710 kB | 0.5/9.0 MB | 366 kB | 49 kB                                                               Downloaded from central: https://repo.maven.apache.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.19.0/jackson-databind-2.19.0.jar (1.7 MB at 2.6 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-junit-jupiter/5.18.0/mockito-junit-jupiter-5.18.0.jar
Progress (4): 527/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 532/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 536/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 540/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 544/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 548/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 552/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 556/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 560/710 kB | 0.5/9.0 MB | 366 kB | 49 kBProgress (4): 560/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 564/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 568/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 573/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 577/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 581/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 585/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 585/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 589/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 593/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 597/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 601/710 kB | 0.6/9.0 MB | 366 kB | 49 kBProgress (4): 601/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 605/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 609/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 613/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 618/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 622/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 626/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 630/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 634/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 638/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 642/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 646/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 650/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 654/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 659/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 663/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 667/710 kB | 0.7/9.0 MB | 366 kB | 49 kBProgress (4): 667/710 kB | 0.7/9.0 MB | 366 kB | 49 kB                                                      Downloaded from central: https://repo.maven.apache.org/maven2/org/objenesis/objenesis/3.3/objenesis-3.3.jar (49 kB at 76 kB/s)
Progress (4): 667/710 kB | 0.7/9.0 MB | 366 kB | 3.1/9.4 kBProgress (4): 667/710 kB | 0.7/9.0 MB | 366 kB | 7.2/9.4 kBProgress (4): 667/710 kB | 0.7/9.0 MB | 366 kB | 9.4 kB                                                           Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy-agent/1.17.5/byte-buddy-agent-1.17.5.jar (366 kB at 556 kB/s)
Progress (3): 671/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 675/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 679/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 683/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 687/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 691/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 695/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 699/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 704/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 708/710 kB | 0.7/9.0 MB | 9.4 kBProgress (3): 710 kB | 0.7/9.0 MB | 9.4 kB    Progress (3): 710 kB | 0.8/9.0 MB | 9.4 kBProgress (3): 710 kB | 0.8/9.0 MB | 9.4 kBProgress (3): 710 kB | 0.9/9.0 MB | 9.4 kBProgress (3): 710 kB | 0.9/9.0 MB | 9.4 kBProgress (3): 710 kB | 1.0/9.0 MB | 9.4 kB                                          Downloaded from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-core/5.18.0/mockito-core-5.18.0.jar (710 kB at 1.0 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/mockito/mockito-junit-jupiter/5.18.0/mockito-junit-jupiter-5.18.0.jar (9.4 kB at 14 kB/s)
Progress (1): 1.0/9.0 MBProgress (1): 1.1/9.0 MBProgress (1): 1.1/9.0 MBProgress (1): 1.2/9.0 MBProgress (1): 1.2/9.0 MBProgress (1): 1.3/9.0 MBProgress (1): 1.3/9.0 MBProgress (1): 1.4/9.0 MBProgress (1): 1.4/9.0 MBProgress (1): 1.5/9.0 MBProgress (1): 1.5/9.0 MBProgress (1): 1.6/9.0 MBProgress (1): 1.6/9.0 MBProgress (1): 1.7/9.0 MBProgress (1): 1.7/9.0 MBProgress (1): 1.8/9.0 MBProgress (1): 1.8/9.0 MBProgress (1): 1.9/9.0 MBProgress (1): 1.9/9.0 MBProgress (1): 2.0/9.0 MBProgress (1): 2.0/9.0 MBProgress (1): 2.1/9.0 MBProgress (1): 2.1/9.0 MBProgress (1): 2.1/9.0 MBProgress (1): 2.2/9.0 MBProgress (1): 2.2/9.0 MBProgress (1): 2.3/9.0 MBProgress (1): 2.3/9.0 MBProgress (1): 2.4/9.0 MBProgress (1): 2.4/9.0 MBProgress (1): 2.5/9.0 MBProgress (1): 2.5/9.0 MBProgress (1): 2.6/9.0 MBProgress (1): 2.6/9.0 MBProgress (1): 2.7/9.0 MBProgress (1): 2.7/9.0 MBProgress (1): 2.8/9.0 MBProgress (1): 2.8/9.0 MBProgress (1): 2.9/9.0 MBProgress (1): 2.9/9.0 MBProgress (1): 3.0/9.0 MBProgress (1): 3.0/9.0 MBProgress (1): 3.1/9.0 MBProgress (1): 3.1/9.0 MBProgress (1): 3.2/9.0 MBProgress (1): 3.2/9.0 MBProgress (1): 3.3/9.0 MBProgress (1): 3.3/9.0 MBProgress (1): 3.4/9.0 MBProgress (1): 3.4/9.0 MBProgress (1): 3.5/9.0 MBProgress (1): 3.5/9.0 MBProgress (1): 3.6/9.0 MBProgress (1): 3.6/9.0 MBProgress (1): 3.7/9.0 MBProgress (1): 3.7/9.0 MBProgress (1): 3.8/9.0 MBProgress (1): 3.8/9.0 MBProgress (1): 3.9/9.0 MBProgress (1): 3.9/9.0 MBProgress (1): 4.0/9.0 MBProgress (1): 4.0/9.0 MBProgress (1): 4.1/9.0 MBProgress (1): 4.1/9.0 MBProgress (1): 4.2/9.0 MBProgress (1): 4.2/9.0 MBProgress (1): 4.3/9.0 MBProgress (1): 4.3/9.0 MBProgress (1): 4.4/9.0 MBProgress (1): 4.4/9.0 MBProgress (1): 4.5/9.0 MBProgress (1): 4.5/9.0 MBProgress (1): 4.6/9.0 MBProgress (1): 4.6/9.0 MBProgress (1): 4.7/9.0 MBProgress (1): 4.7/9.0 MBProgress (1): 4.7/9.0 MBProgress (1): 4.8/9.0 MBProgress (1): 4.8/9.0 MBProgress (1): 4.9/9.0 MBProgress (1): 4.9/9.0 MBProgress (1): 5.0/9.0 MBProgress (1): 5.0/9.0 MBProgress (1): 5.1/9.0 MBProgress (1): 5.1/9.0 MBProgress (1): 5.2/9.0 MBProgress (1): 5.2/9.0 MBProgress (1): 5.3/9.0 MBProgress (1): 5.3/9.0 MBProgress (1): 5.4/9.0 MBProgress (1): 5.4/9.0 MBProgress (1): 5.5/9.0 MBProgress (1): 5.5/9.0 MBProgress (1): 5.6/9.0 MBProgress (1): 5.6/9.0 MBProgress (1): 5.7/9.0 MBProgress (1): 5.7/9.0 MBProgress (1): 5.8/9.0 MBProgress (1): 5.8/9.0 MBProgress (1): 5.9/9.0 MBProgress (1): 5.9/9.0 MBProgress (1): 6.0/9.0 MBProgress (1): 6.0/9.0 MBProgress (1): 6.1/9.0 MBProgress (1): 6.1/9.0 MBProgress (1): 6.2/9.0 MBProgress (1): 6.2/9.0 MBProgress (1): 6.3/9.0 MBProgress (1): 6.3/9.0 MBProgress (1): 6.4/9.0 MBProgress (1): 6.4/9.0 MBProgress (1): 6.5/9.0 MBProgress (1): 6.5/9.0 MBProgress (1): 6.6/9.0 MBProgress (1): 6.6/9.0 MBProgress (1): 6.7/9.0 MBProgress (1): 6.7/9.0 MBProgress (1): 6.8/9.0 MBProgress (1): 6.8/9.0 MBProgress (1): 6.9/9.0 MBProgress (1): 6.9/9.0 MBProgress (1): 7.0/9.0 MBProgress (1): 7.0/9.0 MBProgress (1): 7.0/9.0 MBProgress (1): 7.1/9.0 MBProgress (1): 7.1/9.0 MBProgress (1): 7.2/9.0 MBProgress (1): 7.2/9.0 MBProgress (1): 7.3/9.0 MBProgress (1): 7.3/9.0 MBProgress (1): 7.4/9.0 MBProgress (1): 7.4/9.0 MBProgress (1): 7.5/9.0 MBProgress (1): 7.5/9.0 MBProgress (1): 7.6/9.0 MBProgress (1): 7.6/9.0 MBProgress (1): 7.7/9.0 MBProgress (1): 7.7/9.0 MBProgress (1): 7.8/9.0 MBProgress (1): 7.8/9.0 MBProgress (1): 7.9/9.0 MBProgress (1): 7.9/9.0 MBProgress (1): 8.0/9.0 MBProgress (1): 8.0/9.0 MBProgress (1): 8.1/9.0 MBProgress (1): 8.1/9.0 MBProgress (1): 8.2/9.0 MBProgress (1): 8.2/9.0 MBProgress (1): 8.3/9.0 MBProgress (1): 8.3/9.0 MBProgress (1): 8.4/9.0 MBProgress (1): 8.4/9.0 MBProgress (1): 8.5/9.0 MBProgress (1): 8.5/9.0 MBProgress (1): 8.6/9.0 MBProgress (1): 8.6/9.0 MBProgress (1): 8.7/9.0 MBProgress (1): 8.7/9.0 MBProgress (1): 8.8/9.0 MBProgress (1): 8.8/9.0 MBProgress (1): 8.9/9.0 MBProgress (1): 8.9/9.0 MBProgress (1): 9.0 MB                        Downloaded from central: https://repo.maven.apache.org/maven2/net/bytebuddy/byte-buddy/1.17.5/byte-buddy-1.17.5.jar (9.0 MB at 7.1 MB/s)
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mjacoco-maven-plugin:0.8.13:prepare-agent[m [1m(prepare-agent)[m @ [36mjava-ai-assistant[0;1m ---[m
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.pom
Progress (1): 4.1 kBProgress (1): 4.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.pom (4.1 kB at 129 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/4.0/plexus-4.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 22 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/4.0/plexus-4.0.pom (22 kB at 652 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/10/forge-parent-10.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 14 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/10/forge-parent-10.pom (14 kB at 331 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/file-management/3.1.0/file-management-3.1.0.pom
Progress (1): 4.1 kBProgress (1): 4.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/file-management/3.1.0/file-management-3.1.0.pom (4.5 kB at 121 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/36/maven-shared-components-36.pom
Progress (1): 4.1 kBProgress (1): 4.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/36/maven-shared-components-36.pom (4.9 kB at 144 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/36/maven-parent-36.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 45 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/36/maven-parent-36.pom (45 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/26/apache-26.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 21 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/26/apache-26.pom (21 kB at 513 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.pom
Progress (1): 2.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.pom (2.7 kB at 57 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-parent/1.7.36/slf4j-parent-1.7.36.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 14 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-parent/1.7.36/slf4j-parent-1.7.36.pom (14 kB at 371 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 8.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.4.2/plexus-utils-3.4.2.pom (8.2 kB at 216 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/8/plexus-8.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 25 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/8/plexus-8.pom (25 kB at 636 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.pom (20 kB at 564 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/52/commons-parent-52.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 49 kBProgress (1): 53 kBProgress (1): 57 kBProgress (1): 61 kBProgress (1): 66 kBProgress (1): 70 kBProgress (1): 74 kBProgress (1): 78 kBProgress (1): 79 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/commons/commons-parent/52/commons-parent-52.pom (79 kB at 2.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/23/apache-23.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 18 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/23/apache-23.pom (18 kB at 576 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.7.2/junit-bom-5.7.2.pom
Progress (1): 4.1 kBProgress (1): 5.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.7.2/junit-bom-5.7.2.pom (5.1 kB at 138 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/3.0/maven-reporting-api-3.0.pom
Progress (1): 2.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/3.0/maven-reporting-api-3.0.pom (2.4 kB at 74 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/15/maven-shared-components-15.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/15/maven-shared-components-15.pom (9.3 kB at 252 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/16/maven-parent-16.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 23 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/16/maven-parent-16.pom (23 kB at 665 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/7/apache-7.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 14 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/7/apache-7.pom (14 kB at 437 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0/doxia-sink-api-1.0.pom
Progress (1): 1.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0/doxia-sink-api-1.0.pom (1.4 kB at 34 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.0/doxia-1.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.0/doxia-1.0.pom (9.6 kB at 284 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/10/maven-parent-10.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 32 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/10/maven-parent-10.pom (32 kB at 811 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/4/apache-4.pom
Progress (1): 4.1 kBProgress (1): 4.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/4/apache-4.pom (4.5 kB at 107 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13.pom
Progress (1): 3.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13.pom (3.5 kB at 90 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.core/0.8.13/org.jacoco.core-0.8.13.pom
Progress (1): 2.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.core/0.8.13/org.jacoco.core-0.8.13.pom (2.1 kB at 51 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.pom
Progress (1): 2.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.pom (2.4 kB at 72 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.8/asm-commons-9.8.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.8/asm-commons-9.8.pom (2.8 kB at 77 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.8/asm-tree-9.8.pom
Progress (1): 2.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.8/asm-tree-9.8.pom (2.6 kB at 70 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.report/0.8.13/org.jacoco.report-0.8.13.pom
Progress (1): 1.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.report/0.8.13/org.jacoco.report-0.8.13.pom (1.9 kB at 52 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/file-management/3.1.0/file-management-3.1.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar
Downloading from central: https://repo.maven.apache.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/3.0/maven-reporting-api-3.0.jar
Progress (1): 4.1/247 kBProgress (1): 7.7/247 kBProgress (1): 11/247 kB Progress (1): 15/247 kBProgress (1): 20/247 kBProgress (1): 24/247 kBProgress (1): 28/247 kBProgress (1): 32/247 kBProgress (1): 36/247 kBProgress (1): 40/247 kBProgress (1): 44/247 kBProgress (2): 44/247 kB | 4.1/327 kBProgress (2): 44/247 kB | 7.7/327 kBProgress (3): 44/247 kB | 7.7/327 kB | 4.1/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 7.7/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 12/36 kB Progress (3): 44/247 kB | 7.7/327 kB | 16/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 20/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 24/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 28/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 32/36 kBProgress (3): 44/247 kB | 7.7/327 kB | 36 kB   Progress (4): 44/247 kB | 7.7/327 kB | 36 kB | 4.1/11 kBProgress (4): 44/247 kB | 7.7/327 kB | 36 kB | 7.7/11 kBProgress (4): 44/247 kB | 7.7/327 kB | 36 kB | 11 kB    Progress (4): 44/247 kB | 11/327 kB | 36 kB | 11 kB Progress (4): 44/247 kB | 15/327 kB | 36 kB | 11 kBProgress (4): 44/247 kB | 20/327 kB | 36 kB | 11 kBProgress (4): 44/247 kB | 24/327 kB | 36 kB | 11 kBProgress (4): 44/247 kB | 28/327 kB | 36 kB | 11 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 4.1/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 7.7/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 11/41 kB Progress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 15/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 20/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 24/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 28/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 32/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 36/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 40/41 kBProgress (5): 44/247 kB | 28/327 kB | 36 kB | 11 kB | 41 kB   Progress (5): 48/247 kB | 28/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 52/247 kB | 28/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 56/247 kB | 28/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 28/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 32/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 36/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 40/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 44/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 48/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 52/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 56/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 61/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 65/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 69/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 73/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 61/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 65/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 69/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 73/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 77/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 81/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 85/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 89/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 93/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 97/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 101/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 106/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 110/247 kB | 77/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 110/247 kB | 81/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 110/247 kB | 85/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 110/247 kB | 89/327 kB | 36 kB | 11 kB | 41 kBProgress (5): 110/247 kB | 93/327 kB | 36 kB | 11 kB | 41 kB                                                            Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/file-management/3.1.0/file-management-3.1.0.jar (36 kB at 685 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0/doxia-sink-api-1.0.jar
Progress (4): 110/247 kB | 97/327 kB | 11 kB | 41 kBProgress (4): 110/247 kB | 101/327 kB | 11 kB | 41 kBProgress (4): 110/247 kB | 106/327 kB | 11 kB | 41 kBProgress (4): 110/247 kB | 110/327 kB | 11 kB | 41 kBProgress (4): 110/247 kB | 114/327 kB | 11 kB | 41 kB                                                     Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/3.0/maven-reporting-api-3.0.jar (11 kB at 219 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar
Progress (3): 114/247 kB | 114/327 kB | 41 kBProgress (3): 118/247 kB | 114/327 kB | 41 kBProgress (3): 122/247 kB | 114/327 kB | 41 kBProgress (3): 126/247 kB | 114/327 kB | 41 kBProgress (3): 126/247 kB | 118/327 kB | 41 kBProgress (3): 126/247 kB | 122/327 kB | 41 kBProgress (3): 126/247 kB | 126/327 kB | 41 kBProgress (3): 126/247 kB | 130/327 kB | 41 kBProgress (3): 126/247 kB | 134/327 kB | 41 kBProgress (3): 126/247 kB | 138/327 kB | 41 kBProgress (3): 126/247 kB | 142/327 kB | 41 kBProgress (3): 130/247 kB | 142/327 kB | 41 kBProgress (3): 134/247 kB | 142/327 kB | 41 kBProgress (3): 138/247 kB | 142/327 kB | 41 kBProgress (3): 142/247 kB | 142/327 kB | 41 kBProgress (3): 147/247 kB | 142/327 kB | 41 kBProgress (3): 151/247 kB | 142/327 kB | 41 kBProgress (3): 155/247 kB | 142/327 kB | 41 kBProgress (3): 159/247 kB | 142/327 kB | 41 kBProgress (3): 163/247 kB | 142/327 kB | 41 kBProgress (3): 167/247 kB | 142/327 kB | 41 kBProgress (3): 171/247 kB | 142/327 kB | 41 kBProgress (3): 175/247 kB | 142/327 kB | 41 kBProgress (3): 175/247 kB | 147/327 kB | 41 kBProgress (3): 175/247 kB | 151/327 kB | 41 kBProgress (3): 175/247 kB | 155/327 kB | 41 kBProgress (3): 175/247 kB | 159/327 kB | 41 kBProgress (3): 175/247 kB | 163/327 kB | 41 kBProgress (3): 175/247 kB | 167/327 kB | 41 kBProgress (3): 175/247 kB | 171/327 kB | 41 kBProgress (3): 175/247 kB | 175/327 kB | 41 kBProgress (3): 179/247 kB | 175/327 kB | 41 kBProgress (3): 183/247 kB | 175/327 kB | 41 kBProgress (3): 187/247 kB | 175/327 kB | 41 kBProgress (3): 192/247 kB | 175/327 kB | 41 kBProgress (3): 196/247 kB | 175/327 kB | 41 kBProgress (3): 196/247 kB | 179/327 kB | 41 kBProgress (3): 196/247 kB | 183/327 kB | 41 kBProgress (3): 196/247 kB | 187/327 kB | 41 kBProgress (3): 196/247 kB | 192/327 kB | 41 kBProgress (3): 196/247 kB | 196/327 kB | 41 kBProgress (3): 196/247 kB | 200/327 kB | 41 kBProgress (3): 196/247 kB | 204/327 kB | 41 kBProgress (3): 196/247 kB | 208/327 kB | 41 kBProgress (3): 200/247 kB | 208/327 kB | 41 kBProgress (3): 204/247 kB | 208/327 kB | 41 kBProgress (3): 208/247 kB | 208/327 kB | 41 kBProgress (3): 212/247 kB | 208/327 kB | 41 kBProgress (3): 216/247 kB | 208/327 kB | 41 kBProgress (3): 220/247 kB | 208/327 kB | 41 kBProgress (3): 224/247 kB | 208/327 kB | 41 kBProgress (3): 224/247 kB | 212/327 kB | 41 kBProgress (3): 224/247 kB | 216/327 kB | 41 kBProgress (3): 224/247 kB | 220/327 kB | 41 kBProgress (3): 224/247 kB | 224/327 kB | 41 kBProgress (3): 224/247 kB | 228/327 kB | 41 kBProgress (3): 224/247 kB | 233/327 kB | 41 kBProgress (3): 224/247 kB | 237/327 kB | 41 kBProgress (3): 228/247 kB | 237/327 kB | 41 kBProgress (3): 233/247 kB | 237/327 kB | 41 kBProgress (3): 237/247 kB | 237/327 kB | 41 kBProgress (3): 241/247 kB | 237/327 kB | 41 kBProgress (3): 245/247 kB | 237/327 kB | 41 kBProgress (3): 247 kB | 237/327 kB | 41 kB    Progress (3): 247 kB | 241/327 kB | 41 kBProgress (3): 247 kB | 245/327 kB | 41 kBProgress (3): 247 kB | 249/327 kB | 41 kBProgress (3): 247 kB | 253/327 kB | 41 kBProgress (3): 247 kB | 257/327 kB | 41 kB                                         Downloaded from central: https://repo.maven.apache.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar (41 kB at 579 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.core/0.8.13/org.jacoco.core-0.8.13.jar
Progress (3): 247 kB | 257/327 kB | 4.1/10 kBProgress (3): 247 kB | 261/327 kB | 4.1/10 kBProgress (3): 247 kB | 265/327 kB | 4.1/10 kBProgress (3): 247 kB | 269/327 kB | 4.1/10 kBProgress (3): 247 kB | 274/327 kB | 4.1/10 kBProgress (3): 247 kB | 278/327 kB | 4.1/10 kBProgress (3): 247 kB | 282/327 kB | 4.1/10 kBProgress (3): 247 kB | 286/327 kB | 4.1/10 kBProgress (3): 247 kB | 290/327 kB | 4.1/10 kBProgress (3): 247 kB | 294/327 kB | 4.1/10 kBProgress (3): 247 kB | 298/327 kB | 4.1/10 kBProgress (3): 247 kB | 302/327 kB | 4.1/10 kBProgress (3): 247 kB | 306/327 kB | 4.1/10 kBProgress (3): 247 kB | 310/327 kB | 4.1/10 kBProgress (3): 247 kB | 314/327 kB | 4.1/10 kBProgress (3): 247 kB | 319/327 kB | 4.1/10 kBProgress (3): 247 kB | 323/327 kB | 4.1/10 kBProgress (3): 247 kB | 327/327 kB | 4.1/10 kBProgress (3): 247 kB | 327 kB | 4.1/10 kB    Progress (3): 247 kB | 327 kB | 7.3/10 kBProgress (3): 247 kB | 327 kB | 10 kB    Progress (4): 247 kB | 327 kB | 10 kB | 4.1/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 7.7/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 12/303 kB Progress (4): 247 kB | 327 kB | 10 kB | 15/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 20/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 24/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 28/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 32/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 36/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 40/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 44/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 48/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 52/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 56/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 61/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 65/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 69/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 73/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 77/303 kBProgress (4): 247 kB | 327 kB | 10 kB | 81/303 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 4.1/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 7.7/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 12/223 kB Progress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 16/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 20/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 24/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 81/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 85/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 89/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 93/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 97/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 101/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 106/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 110/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 114/303 kB | 28/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 114/303 kB | 32/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 114/303 kB | 36/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 114/303 kB | 40/223 kBProgress (5): 247 kB | 327 kB | 10 kB | 114/303 kB | 45/223 kB                                                              Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0.24/plexus-utils-3.0.24.jar (247 kB at 2.5 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.jar
Progress (4): 327 kB | 10 kB | 118/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 122/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 126/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 130/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 134/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 138/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 142/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 45/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 49/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 53/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 57/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 61/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 65/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 69/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 73/223 kBProgress (4): 327 kB | 10 kB | 147/303 kB | 77/223 kB                                                     Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0/doxia-sink-api-1.0.jar (10 kB at 108 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/commons-io/commons-io/2.11.0/commons-io-2.11.0.jar (327 kB at 3.3 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar
Progress (2): 147/303 kB | 81/223 kBProgress (2): 147/303 kB | 86/223 kBProgress (2): 147/303 kB | 90/223 kBProgress (2): 147/303 kB | 94/223 kBProgress (2): 147/303 kB | 98/223 kBProgress (2): 147/303 kB | 102/223 kBProgress (2): 147/303 kB | 106/223 kBProgress (2): 147/303 kB | 110/223 kBProgress (2): 147/303 kB | 114/223 kBProgress (2): 147/303 kB | 118/223 kBProgress (2): 147/303 kB | 122/223 kBProgress (2): 151/303 kB | 122/223 kBProgress (2): 155/303 kB | 122/223 kBProgress (2): 159/303 kB | 122/223 kBProgress (2): 163/303 kB | 122/223 kBProgress (2): 167/303 kB | 122/223 kBProgress (2): 171/303 kB | 122/223 kBProgress (2): 175/303 kB | 122/223 kBProgress (2): 179/303 kB | 122/223 kBProgress (2): 183/303 kB | 122/223 kBProgress (2): 187/303 kB | 122/223 kBProgress (2): 192/303 kB | 122/223 kBProgress (2): 196/303 kB | 122/223 kBProgress (2): 196/303 kB | 126/223 kBProgress (2): 196/303 kB | 131/223 kBProgress (2): 196/303 kB | 135/223 kBProgress (2): 196/303 kB | 139/223 kBProgress (2): 196/303 kB | 143/223 kBProgress (2): 200/303 kB | 143/223 kBProgress (2): 204/303 kB | 143/223 kBProgress (2): 208/303 kB | 143/223 kBProgress (2): 212/303 kB | 143/223 kBProgress (2): 216/303 kB | 143/223 kBProgress (2): 220/303 kB | 143/223 kBProgress (2): 224/303 kB | 143/223 kBProgress (2): 228/303 kB | 143/223 kBProgress (2): 228/303 kB | 147/223 kBProgress (2): 228/303 kB | 151/223 kBProgress (2): 228/303 kB | 155/223 kBProgress (2): 228/303 kB | 159/223 kBProgress (2): 228/303 kB | 163/223 kBProgress (2): 228/303 kB | 167/223 kBProgress (2): 228/303 kB | 172/223 kBProgress (2): 228/303 kB | 176/223 kBProgress (2): 233/303 kB | 176/223 kBProgress (2): 237/303 kB | 176/223 kBProgress (2): 241/303 kB | 176/223 kBProgress (2): 245/303 kB | 176/223 kBProgress (2): 249/303 kB | 176/223 kBProgress (2): 253/303 kB | 176/223 kBProgress (2): 257/303 kB | 176/223 kBProgress (2): 261/303 kB | 176/223 kBProgress (2): 261/303 kB | 180/223 kBProgress (2): 261/303 kB | 184/223 kBProgress (2): 261/303 kB | 188/223 kBProgress (2): 261/303 kB | 192/223 kBProgress (2): 261/303 kB | 196/223 kBProgress (2): 261/303 kB | 200/223 kBProgress (2): 261/303 kB | 204/223 kBProgress (2): 261/303 kB | 208/223 kBProgress (2): 265/303 kB | 208/223 kBProgress (2): 269/303 kB | 208/223 kBProgress (2): 274/303 kB | 208/223 kBProgress (2): 278/303 kB | 208/223 kBProgress (2): 278/303 kB | 213/223 kBProgress (2): 278/303 kB | 217/223 kBProgress (2): 278/303 kB | 221/223 kBProgress (2): 278/303 kB | 223 kB    Progress (3): 278/303 kB | 223 kB | 4.1/73 kBProgress (3): 278/303 kB | 223 kB | 8.2/73 kBProgress (3): 278/303 kB | 223 kB | 12/73 kB Progress (3): 278/303 kB | 223 kB | 16/73 kBProgress (3): 282/303 kB | 223 kB | 16/73 kBProgress (3): 286/303 kB | 223 kB | 16/73 kBProgress (3): 290/303 kB | 223 kB | 16/73 kBProgress (3): 294/303 kB | 223 kB | 16/73 kBProgress (3): 298/303 kB | 223 kB | 16/73 kBProgress (3): 302/303 kB | 223 kB | 16/73 kBProgress (3): 303 kB | 223 kB | 16/73 kB    Progress (4): 303 kB | 223 kB | 16/73 kB | 4.1/52 kBProgress (4): 303 kB | 223 kB | 16/73 kB | 7.7/52 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 4.1/126 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 7.7/126 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 12/126 kB Progress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 16/126 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 20/126 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 24/126 kBProgress (5): 303 kB | 223 kB | 16/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 20/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 25/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 29/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 33/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 37/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 7.7/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 12/52 kB | 28/126 kB Progress (5): 303 kB | 223 kB | 41/73 kB | 16/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 20/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 24/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 28/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 32/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 36/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 40/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 45/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 49/52 kB | 28/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 28/126 kB   Progress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 32/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 36/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 40/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 44/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 48/126 kBProgress (5): 303 kB | 223 kB | 41/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 45/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 49/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 53/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 57/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 61/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 66/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 70/73 kB | 52 kB | 52/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 52/126 kB   Progress (5): 303 kB | 223 kB | 73 kB | 52 kB | 56/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 61/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 65/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 69/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 73/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 77/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 81/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 85/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 89/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 93/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 97/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 101/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 106/126 kBProgress (5): 303 kB | 223 kB | 73 kB | 52 kB | 110/126 kB                                                          Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.core/0.8.13/org.jacoco.core-0.8.13.jar (223 kB at 1.7 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.report/0.8.13/org.jacoco.report-0.8.13.jar
Progress (4): 303 kB | 73 kB | 52 kB | 114/126 kBProgress (4): 303 kB | 73 kB | 52 kB | 118/126 kBProgress (4): 303 kB | 73 kB | 52 kB | 122/126 kBProgress (4): 303 kB | 73 kB | 52 kB | 126/126 kBProgress (4): 303 kB | 73 kB | 52 kB | 126 kB                                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar (303 kB at 2.2 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.8/asm-tree-9.8.jar (52 kB at 376 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm-commons/9.8/asm-commons-9.8.jar (73 kB at 510 kB/s)
Progress (2): 126 kB | 4.1/131 kBProgress (2): 126 kB | 7.7/131 kBProgress (2): 126 kB | 11/131 kB Progress (2): 126 kB | 15/131 kBProgress (2): 126 kB | 20/131 kBProgress (2): 126 kB | 24/131 kBProgress (2): 126 kB | 28/131 kBProgress (2): 126 kB | 32/131 kBProgress (2): 126 kB | 36/131 kBProgress (2): 126 kB | 40/131 kBProgress (2): 126 kB | 44/131 kBProgress (2): 126 kB | 48/131 kBProgress (2): 126 kB | 52/131 kBProgress (2): 126 kB | 56/131 kBProgress (2): 126 kB | 61/131 kBProgress (2): 126 kB | 65/131 kBProgress (2): 126 kB | 69/131 kBProgress (2): 126 kB | 73/131 kBProgress (2): 126 kB | 77/131 kBProgress (2): 126 kB | 81/131 kBProgress (2): 126 kB | 85/131 kBProgress (2): 126 kB | 89/131 kBProgress (2): 126 kB | 93/131 kBProgress (2): 126 kB | 97/131 kBProgress (2): 126 kB | 101/131 kBProgress (2): 126 kB | 106/131 kBProgress (2): 126 kB | 110/131 kBProgress (2): 126 kB | 114/131 kBProgress (2): 126 kB | 118/131 kBProgress (2): 126 kB | 122/131 kBProgress (2): 126 kB | 126/131 kBProgress (2): 126 kB | 130/131 kBProgress (2): 126 kB | 131 kB                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.8/asm-9.8.jar (126 kB at 793 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/jacoco/org.jacoco.report/0.8.13/org.jacoco.report-0.8.13.jar (131 kB at 712 kB/s)
[[1;34mINFO[m] argLine set to -javaagent:/root/.m2/repository/org/jacoco/org.jacoco.agent/0.8.13/org.jacoco.agent-0.8.13-runtime.jar=destfile=/root/exp_SWAT/java-ai-assistant/target/jacoco.exec
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-resources-plugin:2.6:resources[m [1m(default-resources)[m @ [36mjava-ai-assistant[0;1m ---[m
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-api/2.0.6/maven-plugin-api-2.0.6.pom
Progress (1): 1.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-api/2.0.6/maven-plugin-api-2.0.6.pom (1.5 kB at 27 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven/2.0.6/maven-2.0.6.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven/2.0.6/maven-2.0.6.pom (9.0 kB at 181 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/5/maven-parent-5.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 15 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/5/maven-parent-5.pom (15 kB at 305 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/3/apache-3.pom
Progress (1): 3.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/3/apache-3.pom (3.4 kB at 88 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-project/2.0.6/maven-project-2.0.6.pom
Progress (1): 2.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-project/2.0.6/maven-project-2.0.6.pom (2.6 kB at 66 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-settings/2.0.6/maven-settings-2.0.6.pom
Progress (1): 2.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-settings/2.0.6/maven-settings-2.0.6.pom (2.0 kB at 44 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-model/2.0.6/maven-model-2.0.6.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-model/2.0.6/maven-model-2.0.6.pom (3.0 kB at 69 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.4.1/plexus-utils-1.4.1.pom
Progress (1): 1.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.4.1/plexus-utils-1.4.1.pom (1.9 kB at 47 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/1.0.11/plexus-1.0.11.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/1.0.11/plexus-1.0.11.pom (9.0 kB at 195 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.0-alpha-9-stable-1/plexus-container-default-1.0-alpha-9-stable-1.pom
Progress (1): 3.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.0-alpha-9-stable-1/plexus-container-default-1.0-alpha-9-stable-1.pom (3.9 kB at 94 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-containers/1.0.3/plexus-containers-1.0.3.pom
Progress (1): 492 B                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-containers/1.0.3/plexus-containers-1.0.3.pom (492 B at 11 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/1.0.4/plexus-1.0.4.pom
Progress (1): 4.1 kBProgress (1): 5.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/1.0.4/plexus-1.0.4.pom (5.7 kB at 133 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.1/junit-3.8.1.pom
Progress (1): 998 B                   Downloaded from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.1/junit-3.8.1.pom (998 B at 21 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.0.4/plexus-utils-1.0.4.pom
Progress (1): 4.1 kBProgress (1): 6.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.0.4/plexus-utils-1.0.4.pom (6.9 kB at 152 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2.pom
Progress (1): 3.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2.pom (3.1 kB at 55 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-profile/2.0.6/maven-profile-2.0.6.pom
Progress (1): 2.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-profile/2.0.6/maven-profile-2.0.6.pom (2.0 kB at 41 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact-manager/2.0.6/maven-artifact-manager-2.0.6.pom
Progress (1): 2.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact-manager/2.0.6/maven-artifact-manager-2.0.6.pom (2.6 kB at 49 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-repository-metadata/2.0.6/maven-repository-metadata-2.0.6.pom
Progress (1): 1.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-repository-metadata/2.0.6/maven-repository-metadata-2.0.6.pom (1.9 kB at 31 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact/2.0.6/maven-artifact-2.0.6.pom
Progress (1): 1.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact/2.0.6/maven-artifact-2.0.6.pom (1.6 kB at 34 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-registry/2.0.6/maven-plugin-registry-2.0.6.pom
Progress (1): 1.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-registry/2.0.6/maven-plugin-registry-2.0.6.pom (1.9 kB at 43 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-core/2.0.6/maven-core-2.0.6.pom
Progress (1): 4.1 kBProgress (1): 6.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-core/2.0.6/maven-core-2.0.6.pom (6.7 kB at 140 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-parameter-documenter/2.0.6/maven-plugin-parameter-documenter-2.0.6.pom
Progress (1): 1.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-parameter-documenter/2.0.6/maven-plugin-parameter-documenter-2.0.6.pom (1.9 kB at 41 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.0.6/maven-reporting-api-2.0.6.pom
Progress (1): 1.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.0.6/maven-reporting-api-2.0.6.pom (1.8 kB at 34 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting/2.0.6/maven-reporting-2.0.6.pom
Progress (1): 1.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting/2.0.6/maven-reporting-2.0.6.pom (1.4 kB at 31 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0-alpha-7/doxia-sink-api-1.0-alpha-7.pom
Progress (1): 424 B                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0-alpha-7/doxia-sink-api-1.0-alpha-7.pom (424 B at 8.5 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.0-alpha-7/doxia-1.0-alpha-7.pom
Progress (1): 3.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia/1.0-alpha-7/doxia-1.0-alpha-7.pom (3.9 kB at 91 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-error-diagnostics/2.0.6/maven-error-diagnostics-2.0.6.pom
Progress (1): 1.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-error-diagnostics/2.0.6/maven-error-diagnostics-2.0.6.pom (1.7 kB at 32 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.0/commons-cli-1.0.pom
Progress (1): 2.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.0/commons-cli-1.0.pom (2.1 kB at 49 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-descriptor/2.0.6/maven-plugin-descriptor-2.0.6.pom
Progress (1): 2.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-descriptor/2.0.6/maven-plugin-descriptor-2.0.6.pom (2.0 kB at 41 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interactivity-api/1.0-alpha-4/plexus-interactivity-api-1.0-alpha-4.pom
Progress (1): 4.1 kBProgress (1): 7.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interactivity-api/1.0-alpha-4/plexus-interactivity-api-1.0-alpha-4.pom (7.1 kB at 142 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-monitor/2.0.6/maven-monitor-2.0.6.pom
Progress (1): 1.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-monitor/2.0.6/maven-monitor-2.0.6.pom (1.3 kB at 28 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1/classworlds-1.1.pom
Progress (1): 3.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1/classworlds-1.1.pom (3.3 kB at 81 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/2.0.5/plexus-utils-2.0.5.pom
Progress (1): 3.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/2.0.5/plexus-utils-2.0.5.pom (3.3 kB at 72 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.6/plexus-2.0.6.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 17 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.6/plexus-2.0.6.pom (17 kB at 381 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-filtering/1.1/maven-filtering-1.1.pom
Progress (1): 4.1 kBProgress (1): 5.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-filtering/1.1/maven-filtering-1.1.pom (5.8 kB at 129 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/17/maven-shared-components-17.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 8.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/17/maven-shared-components-17.pom (8.7 kB at 189 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.5.15/plexus-utils-1.5.15.pom
Progress (1): 4.1 kBProgress (1): 6.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.5.15/plexus-utils-1.5.15.pom (6.8 kB at 143 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.2/plexus-2.0.2.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.2/plexus-2.0.2.pom (12 kB at 283 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.12/plexus-interpolation-1.12.pom
Progress (1): 889 B                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.12/plexus-interpolation-1.12.pom (889 B at 22 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.14/plexus-components-1.1.14.pom
Progress (1): 4.1 kBProgress (1): 5.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.14/plexus-components-1.1.14.pom (5.8 kB at 127 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/plexus/plexus-build-api/0.0.4/plexus-build-api-0.0.4.pom
Progress (1): 2.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/plexus/plexus-build-api/0.0.4/plexus-build-api-0.0.4.pom (2.9 kB at 55 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/10/spice-parent-10.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/10/spice-parent-10.pom (3.0 kB at 66 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/3/forge-parent-3.pom
Progress (1): 4.1 kBProgress (1): 5.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/3/forge-parent-3.pom (5.0 kB at 114 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.5.8/plexus-utils-1.5.8.pom
Progress (1): 4.1 kBProgress (1): 8.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.5.8/plexus-utils-1.5.8.pom (8.1 kB at 202 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.13/plexus-interpolation-1.13.pom
Progress (1): 890 B                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.13/plexus-interpolation-1.13.pom (890 B at 21 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.15/plexus-components-1.1.15.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.15/plexus-components-1.1.15.pom (2.8 kB at 59 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.3/plexus-2.0.3.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 15 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/2.0.3/plexus-2.0.3.pom (15 kB at 351 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-api/2.0.6/maven-plugin-api-2.0.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-project/2.0.6/maven-project-2.0.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact-manager/2.0.6/maven-artifact-manager-2.0.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-profile/2.0.6/maven-profile-2.0.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-registry/2.0.6/maven-plugin-registry-2.0.6.jar
Progress (1): 4.1/13 kBProgress (1): 7.7/13 kBProgress (1): 11/13 kB Progress (1): 13 kB   Progress (2): 13 kB | 4.1/35 kBProgress (3): 13 kB | 4.1/35 kB | 4.1/29 kBProgress (3): 13 kB | 4.1/35 kB | 7.7/29 kBProgress (3): 13 kB | 4.1/35 kB | 12/29 kB Progress (3): 13 kB | 4.1/35 kB | 15/29 kBProgress (3): 13 kB | 4.1/35 kB | 20/29 kBProgress (3): 13 kB | 4.1/35 kB | 24/29 kBProgress (3): 13 kB | 4.1/35 kB | 28/29 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 4.1/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 7.7/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 11/116 kB Progress (4): 13 kB | 4.1/35 kB | 28/29 kB | 15/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 20/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 24/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 28/116 kBProgress (4): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 4.1/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 7.7/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 12/57 kB Progress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 16/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 20/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 24/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 28/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 32/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 36/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 40/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 45/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 49/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 53/57 kBProgress (5): 13 kB | 4.1/35 kB | 28/29 kB | 32/116 kB | 57 kB   Progress (5): 13 kB | 4.1/35 kB | 29 kB | 32/116 kB | 57 kB   Progress (5): 13 kB | 4.1/35 kB | 29 kB | 36/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 40/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 44/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 48/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 52/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 56/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 61/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 65/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 69/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 73/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 77/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 81/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 85/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 89/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 93/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 97/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 101/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 106/116 kB | 57 kBProgress (5): 13 kB | 4.1/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 7.7/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 11/35 kB | 29 kB | 110/116 kB | 57 kB Progress (5): 13 kB | 15/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 20/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 24/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 28/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 32/35 kB | 29 kB | 110/116 kB | 57 kBProgress (5): 13 kB | 35 kB | 29 kB | 110/116 kB | 57 kB   Progress (5): 13 kB | 35 kB | 29 kB | 114/116 kB | 57 kBProgress (5): 13 kB | 35 kB | 29 kB | 116 kB | 57 kB                                                        Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-api/2.0.6/maven-plugin-api-2.0.6.jar (13 kB at 306 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-core/2.0.6/maven-core-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-registry/2.0.6/maven-plugin-registry-2.0.6.jar (29 kB at 657 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-parameter-documenter/2.0.6/maven-plugin-parameter-documenter-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact-manager/2.0.6/maven-artifact-manager-2.0.6.jar (57 kB at 1.2 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.0.6/maven-reporting-api-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-profile/2.0.6/maven-profile-2.0.6.jar (35 kB at 665 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0-alpha-7/doxia-sink-api-1.0-alpha-7.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-project/2.0.6/maven-project-2.0.6.jar (116 kB at 2.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-repository-metadata/2.0.6/maven-repository-metadata-2.0.6.jar
Progress (1): 4.1/152 kBProgress (1): 7.7/152 kBProgress (1): 11/152 kB Progress (1): 15/152 kBProgress (1): 20/152 kBProgress (1): 24/152 kBProgress (1): 28/152 kBProgress (1): 32/152 kBProgress (1): 36/152 kBProgress (1): 40/152 kBProgress (1): 44/152 kBProgress (1): 48/152 kBProgress (1): 52/152 kBProgress (1): 56/152 kBProgress (1): 61/152 kBProgress (1): 65/152 kBProgress (1): 69/152 kBProgress (1): 73/152 kBProgress (1): 77/152 kBProgress (1): 81/152 kBProgress (2): 81/152 kB | 3.2/21 kBProgress (2): 81/152 kB | 7.3/21 kBProgress (2): 81/152 kB | 11/21 kB Progress (2): 81/152 kB | 15/21 kBProgress (2): 81/152 kB | 20/21 kBProgress (2): 81/152 kB | 21 kB   Progress (2): 85/152 kB | 21 kBProgress (2): 89/152 kB | 21 kBProgress (2): 93/152 kB | 21 kBProgress (2): 97/152 kB | 21 kBProgress (2): 101/152 kB | 21 kBProgress (2): 106/152 kB | 21 kBProgress (2): 110/152 kB | 21 kBProgress (2): 114/152 kB | 21 kBProgress (2): 118/152 kB | 21 kBProgress (2): 122/152 kB | 21 kBProgress (2): 126/152 kB | 21 kBProgress (2): 130/152 kB | 21 kBProgress (2): 134/152 kB | 21 kBProgress (2): 138/152 kB | 21 kBProgress (2): 142/152 kB | 21 kBProgress (2): 147/152 kB | 21 kBProgress (2): 151/152 kB | 21 kBProgress (2): 152 kB | 21 kB    Progress (3): 152 kB | 21 kB | 3.1/9.9 kBProgress (3): 152 kB | 21 kB | 7.2/9.9 kBProgress (3): 152 kB | 21 kB | 9.9 kB    Progress (4): 152 kB | 21 kB | 9.9 kB | 4.1/5.9 kBProgress (4): 152 kB | 21 kB | 9.9 kB | 5.9 kB    Progress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 4.1/24 kBProgress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 7.7/24 kBProgress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 11/24 kB Progress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 15/24 kBProgress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 20/24 kBProgress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 24/24 kBProgress (5): 152 kB | 21 kB | 9.9 kB | 5.9 kB | 24 kB                                                         Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-core/2.0.6/maven-core-2.0.6.jar (152 kB at 1.9 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-error-diagnostics/2.0.6/maven-error-diagnostics-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-repository-metadata/2.0.6/maven-repository-metadata-2.0.6.jar (24 kB at 298 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/doxia/doxia-sink-api/1.0-alpha-7/doxia-sink-api-1.0-alpha-7.jar (5.9 kB at 72 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.0/commons-cli-1.0.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-parameter-documenter/2.0.6/maven-plugin-parameter-documenter-2.0.6.jar (21 kB at 247 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-descriptor/2.0.6/maven-plugin-descriptor-2.0.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interactivity-api/1.0-alpha-4/plexus-interactivity-api-1.0-alpha-4.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/reporting/maven-reporting-api/2.0.6/maven-reporting-api-2.0.6.jar (9.9 kB at 117 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1/classworlds-1.1.jar
Progress (1): 4.1/14 kBProgress (1): 7.7/14 kBProgress (1): 12/14 kB Progress (1): 14 kB   Progress (2): 14 kB | 4.1/30 kBProgress (2): 14 kB | 7.7/30 kBProgress (2): 14 kB | 11/30 kB Progress (2): 14 kB | 15/30 kBProgress (2): 14 kB | 20/30 kBProgress (2): 14 kB | 24/30 kBProgress (2): 14 kB | 28/30 kBProgress (2): 14 kB | 30 kB   Progress (3): 14 kB | 30 kB | 4.1/13 kBProgress (3): 14 kB | 30 kB | 7.7/13 kBProgress (3): 14 kB | 30 kB | 11/13 kB Progress (3): 14 kB | 30 kB | 13 kB   Progress (4): 14 kB | 30 kB | 13 kB | 4.1/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 7.7/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 12/38 kB Progress (4): 14 kB | 30 kB | 13 kB | 16/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 20/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 24/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 28/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 32/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 36/38 kBProgress (4): 14 kB | 30 kB | 13 kB | 38 kB   Progress (5): 14 kB | 30 kB | 13 kB | 38 kB | 4.1/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 7.7/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 12/37 kB Progress (5): 14 kB | 30 kB | 13 kB | 38 kB | 16/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 20/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 24/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 28/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 32/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 36/37 kBProgress (5): 14 kB | 30 kB | 13 kB | 38 kB | 37 kB                                                      Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-error-diagnostics/2.0.6/maven-error-diagnostics-2.0.6.jar (14 kB at 118 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact/2.0.6/maven-artifact-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/commons-cli/commons-cli/1.0/commons-cli-1.0.jar (30 kB at 253 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-settings/2.0.6/maven-settings-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interactivity-api/1.0-alpha-4/plexus-interactivity-api-1.0-alpha-4.jar (13 kB at 111 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-model/2.0.6/maven-model-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1/classworlds-1.1.jar (38 kB at 303 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-monitor/2.0.6/maven-monitor-2.0.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-plugin-descriptor/2.0.6/maven-plugin-descriptor-2.0.6.jar (37 kB at 281 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.0-alpha-9-stable-1/plexus-container-default-1.0-alpha-9-stable-1.jar
Progress (1): 3.2/49 kBProgress (1): 7.3/49 kBProgress (1): 11/49 kB Progress (1): 15/49 kBProgress (1): 20/49 kBProgress (1): 24/49 kBProgress (1): 28/49 kBProgress (1): 32/49 kBProgress (1): 36/49 kBProgress (1): 40/49 kBProgress (1): 44/49 kBProgress (1): 48/49 kBProgress (1): 49 kB   Progress (2): 49 kB | 4.1/86 kBProgress (2): 49 kB | 7.7/86 kBProgress (2): 49 kB | 12/86 kB Progress (2): 49 kB | 16/86 kBProgress (2): 49 kB | 20/86 kBProgress (2): 49 kB | 24/86 kBProgress (2): 49 kB | 28/86 kBProgress (2): 49 kB | 32/86 kBProgress (2): 49 kB | 36/86 kBProgress (2): 49 kB | 40/86 kBProgress (2): 49 kB | 45/86 kBProgress (2): 49 kB | 49/86 kBProgress (2): 49 kB | 53/86 kBProgress (2): 49 kB | 57/86 kBProgress (2): 49 kB | 61/86 kBProgress (2): 49 kB | 65/86 kBProgress (2): 49 kB | 69/86 kBProgress (2): 49 kB | 73/86 kBProgress (2): 49 kB | 77/86 kBProgress (2): 49 kB | 81/86 kBProgress (2): 49 kB | 86/86 kBProgress (2): 49 kB | 86 kB   Progress (3): 49 kB | 86 kB | 4.1/87 kBProgress (3): 49 kB | 86 kB | 7.7/87 kBProgress (3): 49 kB | 86 kB | 12/87 kB Progress (3): 49 kB | 86 kB | 16/87 kBProgress (3): 49 kB | 86 kB | 20/87 kBProgress (3): 49 kB | 86 kB | 24/87 kBProgress (3): 49 kB | 86 kB | 28/87 kBProgress (3): 49 kB | 86 kB | 32/87 kBProgress (3): 49 kB | 86 kB | 36/87 kBProgress (3): 49 kB | 86 kB | 40/87 kBProgress (3): 49 kB | 86 kB | 45/87 kBProgress (3): 49 kB | 86 kB | 49/87 kBProgress (4): 49 kB | 86 kB | 49/87 kB | 3.2/10 kBProgress (4): 49 kB | 86 kB | 49/87 kB | 7.3/10 kBProgress (4): 49 kB | 86 kB | 49/87 kB | 10 kB    Progress (4): 49 kB | 86 kB | 53/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 57/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 61/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 65/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 69/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 73/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 77/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 81/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 86/87 kB | 10 kBProgress (4): 49 kB | 86 kB | 87 kB | 10 kB   Progress (5): 49 kB | 86 kB | 87 kB | 10 kB | 4.1/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 7.7/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 12/194 kB Progress (5): 49 kB | 86 kB | 87 kB | 10 kB | 16/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 20/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 24/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 28/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 32/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 36/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 40/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 45/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 49/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 53/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 57/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 61/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 65/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 69/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 73/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 77/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 81/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 85/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 89/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 93/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 97/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 101/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 106/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 110/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 114/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 118/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 122/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 126/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 130/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 134/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 138/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 142/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 147/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 151/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 155/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 159/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 163/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 167/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 171/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 175/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 179/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 183/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 187/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 192/194 kBProgress (5): 49 kB | 86 kB | 87 kB | 10 kB | 194 kB                                                        Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-settings/2.0.6/maven-settings-2.0.6.jar (49 kB at 311 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.1/junit-3.8.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-model/2.0.6/maven-model-2.0.6.jar (86 kB at 537 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/2.0.5/plexus-utils-2.0.5.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-monitor/2.0.6/maven-monitor-2.0.6.jar (10 kB at 64 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-filtering/1.1/maven-filtering-1.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-artifact/2.0.6/maven-artifact-2.0.6.jar (87 kB at 511 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/plexus/plexus-build-api/0.0.4/plexus-build-api-0.0.4.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-container-default/1.0-alpha-9-stable-1/plexus-container-default-1.0-alpha-9-stable-1.jar (194 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.13/plexus-interpolation-1.13.jar
Progress (1): 4.1/223 kBProgress (1): 7.7/223 kBProgress (1): 12/223 kB Progress (1): 16/223 kBProgress (2): 16/223 kB | 4.1/121 kBProgress (2): 16/223 kB | 7.7/121 kBProgress (2): 16/223 kB | 11/121 kB Progress (2): 16/223 kB | 15/121 kBProgress (2): 16/223 kB | 20/121 kBProgress (2): 16/223 kB | 24/121 kBProgress (2): 16/223 kB | 28/121 kBProgress (2): 16/223 kB | 32/121 kBProgress (2): 16/223 kB | 36/121 kBProgress (2): 16/223 kB | 40/121 kBProgress (2): 16/223 kB | 44/121 kBProgress (2): 16/223 kB | 48/121 kBProgress (2): 16/223 kB | 52/121 kBProgress (2): 16/223 kB | 56/121 kBProgress (2): 16/223 kB | 61/121 kBProgress (2): 16/223 kB | 65/121 kBProgress (2): 16/223 kB | 69/121 kBProgress (2): 16/223 kB | 73/121 kBProgress (2): 20/223 kB | 73/121 kBProgress (2): 24/223 kB | 73/121 kBProgress (2): 28/223 kB | 73/121 kBProgress (2): 32/223 kB | 73/121 kBProgress (2): 36/223 kB | 73/121 kBProgress (2): 40/223 kB | 73/121 kBProgress (2): 45/223 kB | 73/121 kBProgress (2): 49/223 kB | 73/121 kBProgress (2): 53/223 kB | 73/121 kBProgress (2): 57/223 kB | 73/121 kBProgress (3): 57/223 kB | 73/121 kB | 4.1/43 kBProgress (3): 57/223 kB | 73/121 kB | 7.7/43 kBProgress (3): 57/223 kB | 73/121 kB | 12/43 kB Progress (3): 57/223 kB | 73/121 kB | 16/43 kBProgress (3): 57/223 kB | 73/121 kB | 20/43 kBProgress (3): 57/223 kB | 73/121 kB | 24/43 kBProgress (3): 57/223 kB | 73/121 kB | 28/43 kBProgress (3): 57/223 kB | 73/121 kB | 32/43 kBProgress (3): 57/223 kB | 73/121 kB | 36/43 kBProgress (3): 57/223 kB | 73/121 kB | 40/43 kBProgress (3): 57/223 kB | 73/121 kB | 43 kB   Progress (3): 61/223 kB | 73/121 kB | 43 kBProgress (3): 65/223 kB | 73/121 kB | 43 kBProgress (3): 69/223 kB | 73/121 kB | 43 kBProgress (3): 73/223 kB | 73/121 kB | 43 kBProgress (3): 77/223 kB | 73/121 kB | 43 kBProgress (3): 81/223 kB | 73/121 kB | 43 kBProgress (3): 86/223 kB | 73/121 kB | 43 kBProgress (3): 90/223 kB | 73/121 kB | 43 kBProgress (3): 94/223 kB | 73/121 kB | 43 kBProgress (3): 98/223 kB | 73/121 kB | 43 kBProgress (3): 102/223 kB | 73/121 kB | 43 kBProgress (3): 106/223 kB | 73/121 kB | 43 kBProgress (3): 110/223 kB | 73/121 kB | 43 kBProgress (3): 114/223 kB | 73/121 kB | 43 kBProgress (3): 118/223 kB | 73/121 kB | 43 kBProgress (3): 122/223 kB | 73/121 kB | 43 kBProgress (3): 126/223 kB | 73/121 kB | 43 kBProgress (3): 131/223 kB | 73/121 kB | 43 kBProgress (3): 135/223 kB | 73/121 kB | 43 kBProgress (3): 139/223 kB | 73/121 kB | 43 kBProgress (3): 139/223 kB | 77/121 kB | 43 kBProgress (3): 139/223 kB | 81/121 kB | 43 kBProgress (3): 139/223 kB | 85/121 kB | 43 kBProgress (3): 139/223 kB | 89/121 kB | 43 kBProgress (3): 139/223 kB | 93/121 kB | 43 kBProgress (3): 139/223 kB | 97/121 kB | 43 kBProgress (3): 139/223 kB | 101/121 kB | 43 kBProgress (3): 139/223 kB | 106/121 kB | 43 kBProgress (3): 139/223 kB | 110/121 kB | 43 kBProgress (3): 139/223 kB | 114/121 kB | 43 kBProgress (3): 139/223 kB | 118/121 kB | 43 kBProgress (3): 139/223 kB | 121 kB | 43 kB    Progress (3): 143/223 kB | 121 kB | 43 kBProgress (3): 147/223 kB | 121 kB | 43 kBProgress (3): 151/223 kB | 121 kB | 43 kBProgress (3): 155/223 kB | 121 kB | 43 kBProgress (3): 159/223 kB | 121 kB | 43 kBProgress (3): 163/223 kB | 121 kB | 43 kBProgress (3): 167/223 kB | 121 kB | 43 kBProgress (3): 171/223 kB | 121 kB | 43 kBProgress (3): 176/223 kB | 121 kB | 43 kBProgress (3): 180/223 kB | 121 kB | 43 kBProgress (3): 184/223 kB | 121 kB | 43 kBProgress (3): 188/223 kB | 121 kB | 43 kBProgress (3): 192/223 kB | 121 kB | 43 kBProgress (3): 196/223 kB | 121 kB | 43 kBProgress (3): 200/223 kB | 121 kB | 43 kBProgress (3): 204/223 kB | 121 kB | 43 kBProgress (3): 208/223 kB | 121 kB | 43 kBProgress (3): 212/223 kB | 121 kB | 43 kBProgress (3): 217/223 kB | 121 kB | 43 kBProgress (3): 221/223 kB | 121 kB | 43 kBProgress (3): 223 kB | 121 kB | 43 kB    Progress (4): 223 kB | 121 kB | 43 kB | 4.1/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 7.7/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 12/61 kB Progress (4): 223 kB | 121 kB | 43 kB | 16/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 20/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 24/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 28/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 32/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 36/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 40/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 45/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 49/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 53/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 57/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 61/61 kBProgress (4): 223 kB | 121 kB | 43 kB | 61 kB                                                Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-filtering/1.1/maven-filtering-1.1.jar (43 kB at 216 kB/s)
Progress (4): 223 kB | 121 kB | 61 kB | 3.2/6.8 kB                                                  Downloaded from central: https://repo.maven.apache.org/maven2/junit/junit/3.8.1/junit-3.8.1.jar (121 kB at 596 kB/s)
Progress (3): 223 kB | 61 kB | 6.8 kB                                     Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/2.0.5/plexus-utils-2.0.5.jar (223 kB at 1.1 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.13/plexus-interpolation-1.13.jar (61 kB at 294 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/plexus/plexus-build-api/0.0.4/plexus-build-api-0.0.4.jar (6.8 kB at 30 kB/s)
[[1;34mINFO[m] Using 'UTF-8' encoding to copy filtered resources.
[[1;34mINFO[m] skip non existing resourceDirectory /root/exp_SWAT/java-ai-assistant/src/main/resources
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-compiler-plugin:3.14.0:compile[m [1m(default-compile)[m @ [36mjava-ai-assistant[0;1m ---[m
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/3.4.2/maven-shared-utils-3.4.2.pom
Progress (1): 4.1 kBProgress (1): 5.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/3.4.2/maven-shared-utils-3.4.2.pom (5.9 kB at 103 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/39/maven-shared-components-39.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/39/maven-shared-components-39.pom (3.2 kB at 83 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/39/maven-parent-39.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 48 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/39/maven-parent-39.pom (48 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/29/apache-29.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 21 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/29/apache-29.pom (21 kB at 441 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-incremental/1.1/maven-shared-incremental-1.1.pom
Progress (1): 4.1 kBProgress (1): 4.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-incremental/1.1/maven-shared-incremental-1.1.pom (4.7 kB at 116 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/19/maven-shared-components-19.pom
Progress (1): 4.1 kBProgress (1): 6.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/19/maven-shared-components-19.pom (6.4 kB at 159 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/23/maven-parent-23.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/23/maven-parent-23.pom (33 kB at 758 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/13/apache-13.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 14 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/13/apache-13.pom (14 kB at 325 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.4.0/plexus-java-1.4.0.pom
Progress (1): 4.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.4.0/plexus-java-1.4.0.pom (4.1 kB at 100 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-languages/1.4.0/plexus-languages-1.4.0.pom
Progress (1): 3.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-languages/1.4.0/plexus-languages-1.4.0.pom (3.9 kB at 88 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/20/plexus-20.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 29 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/20/plexus-20.pom (29 kB at 584 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.pom
Progress (1): 2.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.pom (2.4 kB at 54 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/com/thoughtworks/qdox/qdox/2.2.0/qdox-2.2.0.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 18 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/com/thoughtworks/qdox/qdox/2.2.0/qdox-2.2.0.pom (18 kB at 376 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/oss/oss-parent/9/oss-parent-9.pom
Progress (1): 4.1 kBProgress (1): 6.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/oss/oss-parent/9/oss-parent-9.pom (6.6 kB at 156 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.15.0/plexus-compiler-api-2.15.0.pom
Progress (1): 1.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.15.0/plexus-compiler-api-2.15.0.pom (1.4 kB at 28 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler/2.15.0/plexus-compiler-2.15.0.pom
Progress (1): 4.1 kBProgress (1): 7.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler/2.15.0/plexus-compiler-2.15.0.pom (7.6 kB at 165 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/17/plexus-17.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 28 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/17/plexus-17.pom (28 kB at 587 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.10.2/junit-bom-5.10.2.pom
Progress (1): 4.1 kBProgress (1): 5.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.10.2/junit-bom-5.10.2.pom (5.6 kB at 115 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.pom
Progress (1): 4.1 kBProgress (1): 7.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.pom (7.8 kB at 182 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.15.0/plexus-compiler-manager-2.15.0.pom
Progress (1): 1.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.15.0/plexus-compiler-manager-2.15.0.pom (1.3 kB at 26 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/javax/inject/javax.inject/1/javax.inject-1.pom
Progress (1): 612 B                   Downloaded from central: https://repo.maven.apache.org/maven2/javax/inject/javax.inject/1/javax.inject-1.pom (612 B at 12 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-xml/3.0.1/plexus-xml-3.0.1.pom
Progress (1): 3.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-xml/3.0.1/plexus-xml-3.0.1.pom (3.7 kB at 75 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/18/plexus-18.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 29 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/18/plexus-18.pom (29 kB at 636 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.15.0/plexus-compiler-javac-2.15.0.pom
Progress (1): 1.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.15.0/plexus-compiler-javac-2.15.0.pom (1.3 kB at 29 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compilers/2.15.0/plexus-compilers-2.15.0.pom
Progress (1): 1.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compilers/2.15.0/plexus-compilers-2.15.0.pom (1.6 kB at 27 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/3.4.2/maven-shared-utils-3.4.2.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-incremental/1.1/maven-shared-incremental-1.1.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.4.0/plexus-java-1.4.0.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar
Downloading from central: https://repo.maven.apache.org/maven2/com/thoughtworks/qdox/qdox/2.2.0/qdox-2.2.0.jar
Progress (1): 3.2/14 kBProgress (1): 7.3/14 kBProgress (1): 11/14 kB Progress (1): 14 kB   Progress (2): 14 kB | 3.2/151 kBProgress (2): 14 kB | 7.3/151 kBProgress (2): 14 kB | 11/151 kB Progress (2): 14 kB | 15/151 kBProgress (3): 14 kB | 15/151 kB | 4.1/57 kBProgress (3): 14 kB | 15/151 kB | 7.7/57 kBProgress (3): 14 kB | 15/151 kB | 11/57 kB Progress (3): 14 kB | 15/151 kB | 15/57 kBProgress (3): 14 kB | 15/151 kB | 20/57 kBProgress (3): 14 kB | 15/151 kB | 24/57 kBProgress (3): 14 kB | 15/151 kB | 28/57 kBProgress (3): 14 kB | 15/151 kB | 32/57 kBProgress (3): 14 kB | 15/151 kB | 36/57 kBProgress (3): 14 kB | 15/151 kB | 40/57 kBProgress (3): 14 kB | 15/151 kB | 44/57 kBProgress (3): 14 kB | 15/151 kB | 48/57 kBProgress (3): 14 kB | 15/151 kB | 52/57 kBProgress (3): 14 kB | 15/151 kB | 56/57 kBProgress (3): 14 kB | 15/151 kB | 57 kB   Progress (3): 14 kB | 20/151 kB | 57 kBProgress (3): 14 kB | 24/151 kB | 57 kBProgress (3): 14 kB | 28/151 kB | 57 kBProgress (3): 14 kB | 32/151 kB | 57 kBProgress (3): 14 kB | 36/151 kB | 57 kBProgress (3): 14 kB | 40/151 kB | 57 kBProgress (3): 14 kB | 44/151 kB | 57 kBProgress (3): 14 kB | 48/151 kB | 57 kBProgress (3): 14 kB | 52/151 kB | 57 kBProgress (3): 14 kB | 56/151 kB | 57 kBProgress (3): 14 kB | 61/151 kB | 57 kBProgress (3): 14 kB | 65/151 kB | 57 kBProgress (3): 14 kB | 69/151 kB | 57 kBProgress (3): 14 kB | 73/151 kB | 57 kBProgress (3): 14 kB | 77/151 kB | 57 kBProgress (3): 14 kB | 81/151 kB | 57 kBProgress (3): 14 kB | 85/151 kB | 57 kBProgress (3): 14 kB | 89/151 kB | 57 kBProgress (3): 14 kB | 93/151 kB | 57 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 4.1/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 7.7/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 12/126 kB Progress (4): 14 kB | 93/151 kB | 57 kB | 16/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 20/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 24/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 28/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 32/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 36/126 kBProgress (4): 14 kB | 93/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 97/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 101/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 106/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 110/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 114/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 118/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 122/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 126/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 130/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 134/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 138/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 142/151 kB | 57 kB | 40/126 kBProgress (4): 14 kB | 147/151 kB | 57 kB | 40/126 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 4.1/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 7.7/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 12/353 kB Progress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 16/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 20/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 24/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 28/353 kBProgress (5): 14 kB | 147/151 kB | 57 kB | 40/126 kB | 32/353 kBProgress (5): 14 kB | 151/151 kB | 57 kB | 40/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 40/126 kB | 32/353 kB    Progress (5): 14 kB | 151 kB | 57 kB | 45/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 49/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 53/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 57/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 61/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 65/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 69/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 73/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 77/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 81/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 86/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 90/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 32/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 36/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 40/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 44/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 48/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 52/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 56/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 61/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 65/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 69/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 73/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 77/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 81/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 85/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 89/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 93/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 97/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 101/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 106/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 110/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 114/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 118/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 122/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 126/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 130/353 kBProgress (5): 14 kB | 151 kB | 57 kB | 94/126 kB | 134/353 kB                                                             Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.4.0/plexus-java-1.4.0.jar (57 kB at 1.2 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-incremental/1.1/maven-shared-incremental-1.1.jar (14 kB at 265 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.15.0/plexus-compiler-api-2.15.0.jar
Progress (3): 151 kB | 98/126 kB | 134/353 kBProgress (3): 151 kB | 102/126 kB | 134/353 kBProgress (3): 151 kB | 106/126 kB | 134/353 kBProgress (3): 151 kB | 110/126 kB | 134/353 kBProgress (3): 151 kB | 114/126 kB | 134/353 kBProgress (3): 151 kB | 118/126 kB | 134/353 kBProgress (3): 151 kB | 122/126 kB | 134/353 kBProgress (3): 151 kB | 126 kB | 134/353 kB                                              Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.15.0/plexus-compiler-manager-2.15.0.jar
Progress (3): 151 kB | 126 kB | 138/353 kBProgress (3): 151 kB | 126 kB | 142/353 kBProgress (3): 151 kB | 126 kB | 147/353 kBProgress (3): 151 kB | 126 kB | 151/353 kBProgress (3): 151 kB | 126 kB | 155/353 kBProgress (3): 151 kB | 126 kB | 159/353 kBProgress (3): 151 kB | 126 kB | 163/353 kBProgress (3): 151 kB | 126 kB | 167/353 kBProgress (3): 151 kB | 126 kB | 171/353 kBProgress (3): 151 kB | 126 kB | 175/353 kBProgress (3): 151 kB | 126 kB | 179/353 kBProgress (3): 151 kB | 126 kB | 183/353 kBProgress (3): 151 kB | 126 kB | 187/353 kBProgress (3): 151 kB | 126 kB | 192/353 kBProgress (3): 151 kB | 126 kB | 196/353 kBProgress (3): 151 kB | 126 kB | 200/353 kBProgress (3): 151 kB | 126 kB | 204/353 kBProgress (3): 151 kB | 126 kB | 208/353 kBProgress (3): 151 kB | 126 kB | 212/353 kBProgress (3): 151 kB | 126 kB | 216/353 kBProgress (3): 151 kB | 126 kB | 220/353 kBProgress (3): 151 kB | 126 kB | 224/353 kBProgress (3): 151 kB | 126 kB | 228/353 kBProgress (3): 151 kB | 126 kB | 233/353 kBProgress (3): 151 kB | 126 kB | 237/353 kBProgress (3): 151 kB | 126 kB | 241/353 kBProgress (3): 151 kB | 126 kB | 245/353 kBProgress (3): 151 kB | 126 kB | 249/353 kBProgress (3): 151 kB | 126 kB | 253/353 kBProgress (3): 151 kB | 126 kB | 257/353 kBProgress (3): 151 kB | 126 kB | 261/353 kBProgress (3): 151 kB | 126 kB | 265/353 kBProgress (3): 151 kB | 126 kB | 269/353 kBProgress (3): 151 kB | 126 kB | 274/353 kBProgress (3): 151 kB | 126 kB | 278/353 kBProgress (3): 151 kB | 126 kB | 282/353 kBProgress (3): 151 kB | 126 kB | 286/353 kBProgress (3): 151 kB | 126 kB | 290/353 kBProgress (3): 151 kB | 126 kB | 294/353 kBProgress (3): 151 kB | 126 kB | 298/353 kBProgress (3): 151 kB | 126 kB | 302/353 kBProgress (3): 151 kB | 126 kB | 306/353 kBProgress (3): 151 kB | 126 kB | 310/353 kBProgress (3): 151 kB | 126 kB | 314/353 kBProgress (3): 151 kB | 126 kB | 319/353 kBProgress (3): 151 kB | 126 kB | 323/353 kBProgress (3): 151 kB | 126 kB | 327/353 kBProgress (3): 151 kB | 126 kB | 331/353 kBProgress (3): 151 kB | 126 kB | 335/353 kBProgress (3): 151 kB | 126 kB | 339/353 kBProgress (3): 151 kB | 126 kB | 343/353 kBProgress (3): 151 kB | 126 kB | 347/353 kBProgress (3): 151 kB | 126 kB | 351/353 kBProgress (3): 151 kB | 126 kB | 353 kB    Progress (4): 151 kB | 126 kB | 353 kB | 3.2/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 7.3/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 11/29 kB Progress (4): 151 kB | 126 kB | 353 kB | 15/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 20/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 24/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 28/29 kBProgress (4): 151 kB | 126 kB | 353 kB | 29 kB   Progress (5): 151 kB | 126 kB | 353 kB | 29 kB | 4.1/5.2 kBProgress (5): 151 kB | 126 kB | 353 kB | 29 kB | 5.2 kB                                                           Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-utils/3.4.2/maven-shared-utils-3.4.2.jar (151 kB at 2.0 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.7.1/asm-9.7.1.jar (126 kB at 1.8 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-xml/3.0.1/plexus-xml-3.0.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/com/thoughtworks/qdox/qdox/2.2.0/qdox-2.2.0.jar (353 kB at 4.7 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.15.0/plexus-compiler-javac-2.15.0.jar
Progress (3): 29 kB | 5.2 kB | 2.5 kB                                     Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-api/2.15.0/plexus-compiler-api-2.15.0.jar (29 kB at 350 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-manager/2.15.0/plexus-compiler-manager-2.15.0.jar (5.2 kB at 61 kB/s)
Progress (2): 2.5 kB | 4.1/94 kBProgress (2): 2.5 kB | 7.7/94 kBProgress (2): 2.5 kB | 11/94 kB Progress (2): 2.5 kB | 15/94 kBProgress (2): 2.5 kB | 20/94 kBProgress (2): 2.5 kB | 24/94 kBProgress (2): 2.5 kB | 28/94 kBProgress (2): 2.5 kB | 32/94 kBProgress (2): 2.5 kB | 36/94 kBProgress (2): 2.5 kB | 40/94 kBProgress (2): 2.5 kB | 44/94 kBProgress (2): 2.5 kB | 48/94 kBProgress (2): 2.5 kB | 52/94 kBProgress (2): 2.5 kB | 56/94 kBProgress (2): 2.5 kB | 61/94 kBProgress (2): 2.5 kB | 65/94 kBProgress (2): 2.5 kB | 69/94 kBProgress (2): 2.5 kB | 73/94 kBProgress (3): 2.5 kB | 73/94 kB | 4.1/26 kBProgress (3): 2.5 kB | 73/94 kB | 7.3/26 kBProgress (3): 2.5 kB | 73/94 kB | 11/26 kB Progress (3): 2.5 kB | 73/94 kB | 15/26 kBProgress (3): 2.5 kB | 73/94 kB | 20/26 kBProgress (3): 2.5 kB | 73/94 kB | 24/26 kBProgress (3): 2.5 kB | 73/94 kB | 26 kB   Progress (3): 2.5 kB | 77/94 kB | 26 kBProgress (3): 2.5 kB | 81/94 kB | 26 kBProgress (3): 2.5 kB | 85/94 kB | 26 kBProgress (3): 2.5 kB | 89/94 kB | 26 kBProgress (3): 2.5 kB | 93/94 kB | 26 kBProgress (3): 2.5 kB | 94 kB | 26 kB                                       Downloaded from central: https://repo.maven.apache.org/maven2/javax/inject/javax.inject/1/javax.inject-1.jar (2.5 kB at 24 kB/s)
Progress (3): 94 kB | 26 kB | 3.2/193 kBProgress (3): 94 kB | 26 kB | 7.3/193 kBProgress (3): 94 kB | 26 kB | 11/193 kB Progress (3): 94 kB | 26 kB | 15/193 kBProgress (3): 94 kB | 26 kB | 20/193 kBProgress (3): 94 kB | 26 kB | 24/193 kBProgress (3): 94 kB | 26 kB | 28/193 kBProgress (3): 94 kB | 26 kB | 32/193 kBProgress (3): 94 kB | 26 kB | 36/193 kBProgress (3): 94 kB | 26 kB | 40/193 kBProgress (3): 94 kB | 26 kB | 44/193 kBProgress (3): 94 kB | 26 kB | 48/193 kBProgress (3): 94 kB | 26 kB | 52/193 kBProgress (3): 94 kB | 26 kB | 56/193 kBProgress (3): 94 kB | 26 kB | 61/193 kBProgress (3): 94 kB | 26 kB | 65/193 kBProgress (3): 94 kB | 26 kB | 69/193 kBProgress (3): 94 kB | 26 kB | 73/193 kBProgress (3): 94 kB | 26 kB | 77/193 kBProgress (3): 94 kB | 26 kB | 81/193 kBProgress (3): 94 kB | 26 kB | 85/193 kBProgress (3): 94 kB | 26 kB | 89/193 kBProgress (3): 94 kB | 26 kB | 93/193 kBProgress (3): 94 kB | 26 kB | 97/193 kBProgress (3): 94 kB | 26 kB | 101/193 kBProgress (3): 94 kB | 26 kB | 106/193 kBProgress (3): 94 kB | 26 kB | 110/193 kBProgress (3): 94 kB | 26 kB | 114/193 kBProgress (3): 94 kB | 26 kB | 118/193 kBProgress (3): 94 kB | 26 kB | 122/193 kBProgress (3): 94 kB | 26 kB | 126/193 kBProgress (3): 94 kB | 26 kB | 130/193 kBProgress (3): 94 kB | 26 kB | 134/193 kBProgress (3): 94 kB | 26 kB | 138/193 kBProgress (3): 94 kB | 26 kB | 142/193 kBProgress (3): 94 kB | 26 kB | 147/193 kBProgress (3): 94 kB | 26 kB | 151/193 kBProgress (3): 94 kB | 26 kB | 155/193 kBProgress (3): 94 kB | 26 kB | 159/193 kBProgress (3): 94 kB | 26 kB | 163/193 kBProgress (3): 94 kB | 26 kB | 167/193 kBProgress (3): 94 kB | 26 kB | 171/193 kBProgress (3): 94 kB | 26 kB | 175/193 kBProgress (3): 94 kB | 26 kB | 179/193 kBProgress (3): 94 kB | 26 kB | 183/193 kBProgress (3): 94 kB | 26 kB | 187/193 kBProgress (3): 94 kB | 26 kB | 192/193 kBProgress (3): 94 kB | 26 kB | 193 kB                                        Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-compiler-javac/2.15.0/plexus-compiler-javac-2.15.0.jar (26 kB at 226 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-xml/3.0.1/plexus-xml-3.0.1.jar (94 kB at 793 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/4.0.1/plexus-utils-4.0.1.jar (193 kB at 1.5 MB/s)
[[1;34mINFO[m] Recompiling the module because of [1mchanged dependency[m.
[[1;34mINFO[m] Compiling 3 source files with javac [debug release 17] to target/classes
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-resources-plugin:2.6:testResources[m [1m(default-testResources)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Using 'UTF-8' encoding to copy filtered resources.
[[1;34mINFO[m] skip non existing resourceDirectory /root/exp_SWAT/java-ai-assistant/src/test/resources
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-compiler-plugin:3.14.0:testCompile[m [1m(default-testCompile)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Recompiling the module because of [1mchanged dependency[m.
[[1;34mINFO[m] Compiling 3 source files with javac [debug release 17] to target/test-classes
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-surefire-plugin:3.5.6:test[m [1m(default-test)[m @ [36mjava-ai-assistant[0;1m ---[m
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-api/3.5.6/surefire-api-3.5.6.pom
Progress (1): 3.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-api/3.5.6/surefire-api-3.5.6.pom (3.7 kB at 95 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-logger-api/3.5.6/surefire-logger-api-3.5.6.pom
Progress (1): 3.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-logger-api/3.5.6/surefire-logger-api-3.5.6.pom (3.5 kB at 92 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-shared-utils/3.5.6/surefire-shared-utils-3.5.6.pom
Progress (1): 4.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-shared-utils/3.5.6/surefire-shared-utils-3.5.6.pom (4.0 kB at 101 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-api/3.5.6/surefire-extensions-api-3.5.6.pom
Progress (1): 3.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-api/3.5.6/surefire-extensions-api-3.5.6.pom (3.6 kB at 87 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/maven-surefire-common/3.5.6/maven-surefire-common-3.5.6.pom
Progress (1): 4.1 kBProgress (1): 7.3 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/maven-surefire-common/3.5.6/maven-surefire-common-3.5.6.pom (7.3 kB at 196 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-booter/3.5.6/surefire-booter-3.5.6.pom
Progress (1): 4.1 kBProgress (1): 5.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-booter/3.5.6/surefire-booter-3.5.6.pom (5.1 kB at 122 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-spi/3.5.6/surefire-extensions-spi-3.5.6.pom
Progress (1): 1.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-spi/3.5.6/surefire-extensions-spi-3.5.6.pom (1.7 kB at 46 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-util/1.4.1/maven-resolver-util-1.4.1.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-util/1.4.1/maven-resolver-util-1.4.1.pom (2.8 kB at 80 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver/1.4.1/maven-resolver-1.4.1.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 18 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver/1.4.1/maven-resolver-1.4.1.pom (18 kB at 551 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/33/maven-parent-33.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 44 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/33/maven-parent-33.pom (44 kB at 1.3 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/21/apache-21.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 17 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/21/apache-21.pom (17 kB at 428 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-api/1.4.1/maven-resolver-api-1.4.1.pom
Progress (1): 2.6 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-api/1.4.1/maven-resolver-api-1.4.1.pom (2.6 kB at 64 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-common-artifact-filters/3.4.0/maven-common-artifact-filters-3.4.0.pom
Progress (1): 4.1 kBProgress (1): 5.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-common-artifact-filters/3.4.0/maven-common-artifact-filters-3.4.0.pom (5.4 kB at 149 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/42/maven-shared-components-42.pom
Progress (1): 3.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-shared-components/42/maven-shared-components-42.pom (3.8 kB at 111 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/42/maven-parent-42.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 33 kBProgress (1): 37 kBProgress (1): 41 kBProgress (1): 45 kBProgress (1): 49 kBProgress (1): 50 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-parent/42/maven-parent-42.pom (50 kB at 1.4 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/apache/32/apache-32.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 24 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/apache/32/apache-32.pom (24 kB at 636 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.5.2/plexus-java-1.5.2.pom
Progress (1): 4.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.5.2/plexus-java-1.5.2.pom (4.1 kB at 117 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-languages/1.5.2/plexus-languages-1.5.2.pom
Progress (1): 3.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-languages/1.5.2/plexus-languages-1.5.2.pom (3.9 kB at 124 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/24/plexus-24.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 20 kBProgress (1): 25 kBProgress (1): 29 kBProgress (1): 31 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/24/plexus-24.pom (31 kB at 805 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.13.4/junit-bom-5.13.4.pom
Progress (1): 4.1 kBProgress (1): 5.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/junit-bom/5.13.4/junit-bom-5.13.4.pom (5.7 kB at 149 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.9.1/asm-9.9.1.pom
Progress (1): 2.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.9.1/asm-9.9.1.pom (2.4 kB at 64 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-api/3.5.6/surefire-api-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-logger-api/3.5.6/surefire-logger-api-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-shared-utils/3.5.6/surefire-shared-utils-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-api/3.5.6/surefire-extensions-api-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/maven-surefire-common/3.5.6/maven-surefire-common-3.5.6.jar
Progress (1): 3.2/174 kBProgress (1): 7.3/174 kBProgress (1): 11/174 kB Progress (1): 15/174 kBProgress (1): 20/174 kBProgress (1): 24/174 kBProgress (1): 28/174 kBProgress (1): 32/174 kBProgress (1): 36/174 kBProgress (1): 40/174 kBProgress (1): 44/174 kBProgress (1): 48/174 kBProgress (1): 52/174 kBProgress (1): 56/174 kBProgress (1): 61/174 kBProgress (1): 65/174 kBProgress (1): 69/174 kBProgress (1): 73/174 kBProgress (1): 77/174 kBProgress (1): 81/174 kBProgress (1): 85/174 kBProgress (1): 89/174 kBProgress (1): 93/174 kBProgress (1): 97/174 kBProgress (1): 101/174 kBProgress (1): 106/174 kBProgress (1): 110/174 kBProgress (1): 114/174 kBProgress (1): 118/174 kBProgress (1): 122/174 kBProgress (1): 126/174 kBProgress (1): 130/174 kBProgress (1): 134/174 kBProgress (1): 138/174 kBProgress (1): 142/174 kBProgress (2): 142/174 kB | 4.1/26 kBProgress (2): 142/174 kB | 7.7/26 kBProgress (2): 142/174 kB | 12/26 kB Progress (2): 142/174 kB | 15/26 kBProgress (2): 142/174 kB | 20/26 kBProgress (2): 142/174 kB | 24/26 kBProgress (2): 142/174 kB | 26 kB   Progress (3): 142/174 kB | 26 kB | 0/3.0 MBProgress (3): 147/174 kB | 26 kB | 0/3.0 MBProgress (3): 151/174 kB | 26 kB | 0/3.0 MBProgress (3): 155/174 kB | 26 kB | 0/3.0 MBProgress (3): 159/174 kB | 26 kB | 0/3.0 MBProgress (3): 163/174 kB | 26 kB | 0/3.0 MBProgress (3): 167/174 kB | 26 kB | 0/3.0 MBProgress (3): 171/174 kB | 26 kB | 0/3.0 MBProgress (3): 174 kB | 26 kB | 0/3.0 MB    Progress (4): 174 kB | 26 kB | 0/3.0 MB | 4.1/14 kBProgress (4): 174 kB | 26 kB | 0/3.0 MB | 7.7/14 kBProgress (4): 174 kB | 26 kB | 0/3.0 MB | 11/14 kB Progress (4): 174 kB | 26 kB | 0/3.0 MB | 14 kB   Progress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 3.2/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 7.3/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 11/317 kB Progress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 15/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 20/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 24/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 28/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 32/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 36/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 40/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 44/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 48/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.1/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.2/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 52/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 56/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 61/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 65/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 69/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 73/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 77/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 81/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 85/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 89/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 93/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 97/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 101/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 106/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 110/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 114/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 118/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 122/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 126/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 130/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 134/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 138/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 142/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 147/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 151/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 155/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 159/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 163/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 167/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 171/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 175/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 179/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 183/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 187/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 192/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 196/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 200/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 204/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 208/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 212/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 216/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 220/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 224/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.3/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 228/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 233/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 237/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 241/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 245/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 249/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 253/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 257/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 261/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 265/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 269/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 274/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 278/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 282/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 286/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 290/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 294/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 298/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 302/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 306/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 310/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 314/317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kB    Progress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.4/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.5/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.6/3.0 MB | 14 kB | 317 kBProgress (5): 174 kB | 26 kB | 0.6/3.0 MB | 14 kB | 317 kB                                                          Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-logger-api/3.5.6/surefire-logger-api-3.5.6.jar (14 kB at 126 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-api/3.5.6/surefire-extensions-api-3.5.6.jar (26 kB at 241 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-booter/3.5.6/surefire-booter-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-spi/3.5.6/surefire-extensions-spi-3.5.6.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-api/3.5.6/surefire-api-3.5.6.jar (174 kB at 1.5 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-util/1.4.1/maven-resolver-util-1.4.1.jar
Progress (2): 0.6/3.0 MB | 317 kBProgress (2): 0.6/3.0 MB | 317 kBProgress (2): 0.6/3.0 MB | 317 kBProgress (2): 0.6/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.7/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.8/3.0 MB | 317 kBProgress (2): 0.9/3.0 MB | 317 kBProgress (2): 0.9/3.0 MB | 317 kBProgress (2): 0.9/3.0 MB | 317 kBProgress (2): 0.9/3.0 MB | 317 kB                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/maven-surefire-common/3.5.6/maven-surefire-common-3.5.6.jar (317 kB at 2.7 MB/s)
Progress (1): 0.9/3.0 MBProgress (1): 0.9/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.0/3.0 MBProgress (1): 1.1/3.0 MB                        Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-api/1.4.1/maven-resolver-api-1.4.1.jar
Progress (1): 1.1/3.0 MBProgress (1): 1.1/3.0 MBProgress (1): 1.1/3.0 MBProgress (1): 1.1/3.0 MBProgress (1): 1.1/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.2/3.0 MBProgress (1): 1.3/3.0 MBProgress (2): 1.3/3.0 MB | 3.2/8.2 kBProgress (2): 1.3/3.0 MB | 7.3/8.2 kBProgress (2): 1.3/3.0 MB | 8.2 kB    Progress (3): 1.3/3.0 MB | 8.2 kB | 4.1/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 7.7/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 12/123 kB Progress (3): 1.3/3.0 MB | 8.2 kB | 16/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 20/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 24/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 28/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 32/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 36/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 40/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 45/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 49/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.3/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 53/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 57/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 61/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 65/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 69/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 73/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 77/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 81/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 86/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 90/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 94/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 98/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 102/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 106/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 110/123 kBProgress (3): 1.4/3.0 MB | 8.2 kB | 114/123 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 4.1/168 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 7.7/168 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 12/168 kB Progress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 16/168 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 20/168 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 24/168 kBProgress (4): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 28/168 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 28/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 32/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 36/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 40/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 45/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 49/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 53/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 114/123 kB | 57/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 118/123 kB | 57/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 122/123 kB | 57/168 kB | 4.1/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 4.1/149 kB    Progress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 7.7/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 12/149 kB Progress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 16/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 20/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 57/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 61/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 65/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 69/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 73/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 77/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 81/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 86/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 90/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 94/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 98/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 102/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 106/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 110/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 114/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 118/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 122/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 126/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 131/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 135/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 139/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 143/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 147/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 151/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 155/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 159/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 163/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 167/168 kB | 24/149 kBProgress (5): 1.4/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kB    Progress (5): 1.5/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.5/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.5/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.5/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.5/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 24/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 28/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 32/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 36/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 40/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 45/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 49/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 53/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 57/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 61/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 65/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 69/149 kBProgress (5): 1.6/3.0 MB | 8.2 kB | 123 kB | 168 kB | 73/149 kB                                                               Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-extensions-spi/3.5.6/surefire-extensions-spi-3.5.6.jar (8.2 kB at 56 kB/s)
Progress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kBProgress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kBProgress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kBProgress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kBProgress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kBProgress (4): 1.6/3.0 MB | 123 kB | 168 kB | 73/149 kB                                                      Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-common-artifact-filters/3.4.0/maven-common-artifact-filters-3.4.0.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-booter/3.5.6/surefire-booter-3.5.6.jar (123 kB at 821 kB/s)
Progress (3): 1.6/3.0 MB | 168 kB | 77/149 kBProgress (3): 1.6/3.0 MB | 168 kB | 81/149 kBProgress (3): 1.6/3.0 MB | 168 kB | 86/149 kBProgress (3): 1.6/3.0 MB | 168 kB | 90/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 90/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 90/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 90/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 90/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 90/149 kB                                             Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.5.2/plexus-java-1.5.2.jar
Progress (3): 1.7/3.0 MB | 168 kB | 94/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 98/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 102/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 106/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 110/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 114/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 118/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 122/149 kBProgress (3): 1.7/3.0 MB | 168 kB | 122/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 122/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 122/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 122/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 126/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 131/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 135/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 139/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 139/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 139/149 kBProgress (3): 1.8/3.0 MB | 168 kB | 139/149 kBProgress (3): 1.9/3.0 MB | 168 kB | 139/149 kBProgress (3): 1.9/3.0 MB | 168 kB | 139/149 kB                                              Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-util/1.4.1/maven-resolver-util-1.4.1.jar (168 kB at 1.0 MB/s)
Progress (2): 1.9/3.0 MB | 143/149 kBProgress (2): 1.9/3.0 MB | 147/149 kBProgress (2): 1.9/3.0 MB | 149 kB    Progress (2): 1.9/3.0 MB | 149 kBProgress (2): 1.9/3.0 MB | 149 kBProgress (2): 1.9/3.0 MB | 149 kB                                 Downloading from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.9.1/asm-9.9.1.jar
Progress (2): 1.9/3.0 MB | 149 kBProgress (2): 2.0/3.0 MB | 149 kBProgress (2): 2.0/3.0 MB | 149 kBProgress (3): 2.0/3.0 MB | 149 kB | 4.1/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 7.7/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 11/58 kB Progress (3): 2.0/3.0 MB | 149 kB | 15/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 20/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 24/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 28/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 32/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 36/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 40/58 kBProgress (3): 2.0/3.0 MB | 149 kB | 44/58 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 4.1/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 7.7/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 12/57 kB Progress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 16/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 20/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 24/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 28/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 32/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 36/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 40/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 45/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 49/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 53/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57/57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57 kB   Progress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.0/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 44/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 48/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 52/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 56/58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 58 kB | 57 kB   Progress (4): 2.1/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.1/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.2/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.3/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.4/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.5/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.5/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.5/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.5/3.0 MB | 149 kB | 58 kB | 57 kBProgress (4): 2.5/3.0 MB | 149 kB | 58 kB | 57 kB                                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/resolver/maven-resolver-api/1.4.1/maven-resolver-api-1.4.1.jar (149 kB at 832 kB/s)
Progress (3): 2.5/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kB                                        Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar
Progress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.6/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.7/3.0 MB | 58 kB | 57 kBProgress (3): 2.8/3.0 MB | 58 kB | 57 kBProgress (3): 2.8/3.0 MB | 58 kB | 57 kBProgress (3): 2.8/3.0 MB | 58 kB | 57 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 4.1/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 7.7/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 12/126 kB Progress (4): 2.8/3.0 MB | 58 kB | 57 kB | 16/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 20/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 24/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 28/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 32/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 36/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 40/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 45/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 49/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 53/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 57/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 61/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 65/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 69/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 73/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 77/126 kBProgress (4): 2.8/3.0 MB | 58 kB | 57 kB | 81/126 kB                                                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-java/1.5.2/plexus-java-1.5.2.jar (57 kB at 305 kB/s)
Progress (3): 2.8/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.8/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.8/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 81/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 86/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 90/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 94/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 98/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 102/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 106/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 110/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 114/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 118/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 122/126 kBProgress (3): 2.9/3.0 MB | 58 kB | 126 kB                                             Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/shared/maven-common-artifact-filters/3.4.0/maven-common-artifact-filters-3.4.0.jar (58 kB at 306 kB/s)
Progress (2): 2.9/3.0 MB | 126 kBProgress (2): 3.0/3.0 MB | 126 kBProgress (2): 3.0/3.0 MB | 126 kBProgress (2): 3.0/3.0 MB | 126 kBProgress (2): 3.0 MB | 126 kB    Progress (3): 3.0 MB | 126 kB | 4.1/169 kBProgress (3): 3.0 MB | 126 kB | 7.7/169 kBProgress (3): 3.0 MB | 126 kB | 12/169 kB Progress (3): 3.0 MB | 126 kB | 16/169 kBProgress (3): 3.0 MB | 126 kB | 20/169 kBProgress (3): 3.0 MB | 126 kB | 24/169 kBProgress (3): 3.0 MB | 126 kB | 28/169 kBProgress (3): 3.0 MB | 126 kB | 32/169 kBProgress (3): 3.0 MB | 126 kB | 36/169 kBProgress (3): 3.0 MB | 126 kB | 40/169 kBProgress (3): 3.0 MB | 126 kB | 45/169 kBProgress (3): 3.0 MB | 126 kB | 49/169 kBProgress (3): 3.0 MB | 126 kB | 53/169 kBProgress (3): 3.0 MB | 126 kB | 57/169 kBProgress (3): 3.0 MB | 126 kB | 61/169 kBProgress (3): 3.0 MB | 126 kB | 65/169 kBProgress (3): 3.0 MB | 126 kB | 69/169 kBProgress (3): 3.0 MB | 126 kB | 73/169 kBProgress (3): 3.0 MB | 126 kB | 77/169 kBProgress (3): 3.0 MB | 126 kB | 81/169 kBProgress (3): 3.0 MB | 126 kB | 86/169 kBProgress (3): 3.0 MB | 126 kB | 90/169 kBProgress (3): 3.0 MB | 126 kB | 94/169 kBProgress (3): 3.0 MB | 126 kB | 98/169 kBProgress (3): 3.0 MB | 126 kB | 102/169 kBProgress (3): 3.0 MB | 126 kB | 106/169 kBProgress (3): 3.0 MB | 126 kB | 110/169 kBProgress (3): 3.0 MB | 126 kB | 114/169 kBProgress (3): 3.0 MB | 126 kB | 118/169 kBProgress (3): 3.0 MB | 126 kB | 122/169 kBProgress (3): 3.0 MB | 126 kB | 126/169 kBProgress (3): 3.0 MB | 126 kB | 131/169 kBProgress (3): 3.0 MB | 126 kB | 135/169 kBProgress (3): 3.0 MB | 126 kB | 139/169 kBProgress (3): 3.0 MB | 126 kB | 143/169 kBProgress (3): 3.0 MB | 126 kB | 147/169 kBProgress (3): 3.0 MB | 126 kB | 151/169 kBProgress (3): 3.0 MB | 126 kB | 155/169 kBProgress (3): 3.0 MB | 126 kB | 159/169 kB                                          Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-shared-utils/3.5.6/surefire-shared-utils-3.5.6.jar (3.0 MB at 14 MB/s)
Progress (2): 126 kB | 163/169 kBProgress (2): 126 kB | 167/169 kBProgress (2): 126 kB | 169 kB                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/ow2/asm/asm/9.9.1/asm-9.9.1.jar (126 kB at 579 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/1.1/plexus-utils-1.1.jar (169 kB at 736 kB/s)
[[1;34mINFO[m] Using auto detected provider org.apache.maven.surefire.junitplatform.JUnitPlatformProvider
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-junit-platform/3.5.6/surefire-junit-platform-3.5.6.pom
Progress (1): 4.1 kBProgress (1): 5.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-junit-platform/3.5.6/surefire-junit-platform-3.5.6.pom (5.2 kB at 110 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-providers/3.5.6/surefire-providers-3.5.6.pom
Progress (1): 2.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-providers/3.5.6/surefire-providers-3.5.6.pom (2.5 kB at 59 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/common-java5/3.5.6/common-java5-3.5.6.pom
Progress (1): 3.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/common-java5/3.5.6/common-java5-3.5.6.pom (3.1 kB at 99 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.12.2/junit-platform-engine-1.12.2.pom
Progress (1): 3.2 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.12.2/junit-platform-engine-1.12.2.pom (3.2 kB at 80 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.12.2/junit-platform-commons-1.12.2.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.12.2/junit-platform-commons-1.12.2.pom (2.8 kB at 75 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.12.2/junit-platform-launcher-1.12.2.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.12.2/junit-platform-launcher-1.12.2.pom (3.0 kB at 87 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-junit-platform/3.5.6/surefire-junit-platform-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/common-java5/3.5.6/common-java5-3.5.6.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.12.2/junit-platform-engine-1.12.2.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.12.2/junit-platform-commons-1.12.2.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.12.2/junit-platform-launcher-1.12.2.jar
Progress (1): 4.1/35 kBProgress (1): 7.7/35 kBProgress (1): 12/35 kB Progress (1): 15/35 kBProgress (1): 20/35 kBProgress (1): 24/35 kBProgress (1): 28/35 kBProgress (1): 32/35 kBProgress (1): 35 kB   Progress (2): 35 kB | 4.1/152 kBProgress (2): 35 kB | 7.7/152 kBProgress (2): 35 kB | 12/152 kB Progress (3): 35 kB | 12/152 kB | 4.1/18 kBProgress (3): 35 kB | 12/152 kB | 7.7/18 kBProgress (3): 35 kB | 12/152 kB | 11/18 kB Progress (3): 35 kB | 12/152 kB | 15/18 kBProgress (3): 35 kB | 12/152 kB | 18 kB   Progress (3): 35 kB | 16/152 kB | 18 kBProgress (3): 35 kB | 20/152 kB | 18 kBProgress (3): 35 kB | 24/152 kB | 18 kBProgress (3): 35 kB | 28/152 kB | 18 kBProgress (3): 35 kB | 32/152 kB | 18 kBProgress (3): 35 kB | 36/152 kB | 18 kBProgress (3): 35 kB | 40/152 kB | 18 kBProgress (3): 35 kB | 45/152 kB | 18 kBProgress (3): 35 kB | 49/152 kB | 18 kBProgress (3): 35 kB | 53/152 kB | 18 kBProgress (3): 35 kB | 57/152 kB | 18 kBProgress (3): 35 kB | 61/152 kB | 18 kBProgress (3): 35 kB | 65/152 kB | 18 kBProgress (3): 35 kB | 69/152 kB | 18 kBProgress (3): 35 kB | 73/152 kB | 18 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 4.1/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 7.7/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 11/208 kB Progress (4): 35 kB | 73/152 kB | 18 kB | 15/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 20/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 24/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 28/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 32/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 36/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 40/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 44/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 48/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 52/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 56/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 61/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 65/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 69/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 73/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 77/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 81/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 85/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 89/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 93/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 97/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 101/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 106/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 110/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 114/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 118/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 122/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 126/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 130/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 134/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 138/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 142/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 147/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 151/208 kBProgress (4): 35 kB | 73/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 77/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 81/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 85/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 89/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 93/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 97/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 101/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 106/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 110/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 114/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 118/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 122/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 126/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 130/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 134/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 138/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 142/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 147/152 kB | 18 kB | 155/208 kBProgress (4): 35 kB | 151/152 kB | 18 kB | 155/208 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 4.1/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 7.7/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 12/256 kB Progress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 16/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 20/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 24/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 28/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 32/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 36/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 40/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 44/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 48/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 52/256 kBProgress (5): 35 kB | 151/152 kB | 18 kB | 155/208 kB | 56/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 56/256 kB    Progress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 60/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 65/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 69/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 73/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 77/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 81/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 85/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 89/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 93/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 97/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 101/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 106/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 110/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 114/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 118/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 155/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 159/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 163/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 167/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 171/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 175/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 179/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 183/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 187/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 192/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 196/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 200/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 204/208 kB | 122/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 122/256 kB    Progress (5): 35 kB | 152 kB | 18 kB | 208 kB | 126/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 130/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 134/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 138/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 142/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 147/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 151/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 155/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 159/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 163/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 167/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 171/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 175/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 179/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 183/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 187/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 192/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 196/256 kBProgress (5): 35 kB | 152 kB | 18 kB | 208 kB | 200/256 kB                                                          Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/surefire-junit-platform/3.5.6/surefire-junit-platform-3.5.6.jar (35 kB at 865 kB/s)
Progress (4): 152 kB | 18 kB | 208 kB | 204/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 208/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 212/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 216/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 220/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 224/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 228/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 233/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 237/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 241/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 245/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 249/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 253/256 kBProgress (4): 152 kB | 18 kB | 208 kB | 256 kB                                                  Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/surefire/common-java5/3.5.6/common-java5-3.5.6.jar (18 kB at 420 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-commons/1.12.2/junit-platform-commons-1.12.2.jar (152 kB at 3.4 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.12.2/junit-platform-launcher-1.12.2.jar (208 kB at 4.3 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-engine/1.12.2/junit-platform-engine-1.12.2.jar (256 kB at 4.3 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.14.4/junit-platform-launcher-1.14.4.pom
Progress (1): 3.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.14.4/junit-platform-launcher-1.14.4.pom (3.0 kB at 98 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.14.4/junit-platform-launcher-1.14.4.jar
Progress (1): 4.1/223 kBProgress (1): 7.7/223 kBProgress (1): 12/223 kB Progress (1): 15/223 kBProgress (1): 20/223 kBProgress (1): 24/223 kBProgress (1): 28/223 kBProgress (1): 32/223 kBProgress (1): 36/223 kBProgress (1): 40/223 kBProgress (1): 44/223 kBProgress (1): 48/223 kBProgress (1): 52/223 kBProgress (1): 56/223 kBProgress (1): 61/223 kBProgress (1): 65/223 kBProgress (1): 69/223 kBProgress (1): 73/223 kBProgress (1): 77/223 kBProgress (1): 81/223 kBProgress (1): 85/223 kBProgress (1): 89/223 kBProgress (1): 93/223 kBProgress (1): 97/223 kBProgress (1): 101/223 kBProgress (1): 106/223 kBProgress (1): 110/223 kBProgress (1): 114/223 kBProgress (1): 118/223 kBProgress (1): 122/223 kBProgress (1): 126/223 kBProgress (1): 130/223 kBProgress (1): 134/223 kBProgress (1): 138/223 kBProgress (1): 142/223 kBProgress (1): 147/223 kBProgress (1): 151/223 kBProgress (1): 155/223 kBProgress (1): 159/223 kBProgress (1): 163/223 kBProgress (1): 167/223 kBProgress (1): 171/223 kBProgress (1): 175/223 kBProgress (1): 179/223 kBProgress (1): 183/223 kBProgress (1): 187/223 kBProgress (1): 192/223 kBProgress (1): 196/223 kBProgress (1): 200/223 kBProgress (1): 204/223 kBProgress (1): 208/223 kBProgress (1): 212/223 kBProgress (1): 216/223 kBProgress (1): 220/223 kBProgress (1): 223 kB                        Downloaded from central: https://repo.maven.apache.org/maven2/org/junit/platform/junit-platform-launcher/1.14.4/junit-platform-launcher-1.14.4.jar (223 kB at 5.1 MB/s)
[[1;34mINFO[m] 
[[1;34mINFO[m] -------------------------------------------------------
[[1;34mINFO[m]  T E S T S
[[1;34mINFO[m] -------------------------------------------------------
[[1;34mINFO[m] Running assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m6[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.334 s -- in assistant.common.[1mBusinessExceptionTest[m
[[1;34mINFO[m] Running assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m2[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.049 s -- in assistant.common.[1mErrorCodeTest[m
[[1;34mINFO[m] Running assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] [1;32mTests run: [0;1;32m9[m, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.144 s -- in assistant.common.[1mOperationResultTest[m
[[1;34mINFO[m] 
[[1;34mINFO[m] Results:
[[1;34mINFO[m] 
[[1;34mINFO[m] [1;32mTests run: 17, Failures: 0, Errors: 0, Skipped: 0[m
[[1;34mINFO[m] 
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mmaven-jar-plugin:2.4:jar[m [1m(default-jar)[m @ [36mjava-ai-assistant[0;1m ---[m
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-archiver/2.5/maven-archiver-2.5.pom
Progress (1): 4.1 kBProgress (1): 4.5 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-archiver/2.5/maven-archiver-2.5.pom (4.5 kB at 133 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-archiver/2.1/plexus-archiver-2.1.pom
Progress (1): 2.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-archiver/2.1/plexus-archiver-2.1.pom (2.8 kB at 76 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/17/spice-parent-17.pom
Progress (1): 4.1 kBProgress (1): 6.8 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/17/spice-parent-17.pom (6.8 kB at 225 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0/plexus-utils-3.0.pom
Progress (1): 4.1 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0/plexus-utils-3.0.pom (4.1 kB at 127 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/16/spice-parent-16.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 8.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/spice/spice-parent/16/spice-parent-16.pom (8.4 kB at 270 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/5/forge-parent-5.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 8.4 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/sonatype/forge/forge-parent/5/forge-parent-5.pom (8.4 kB at 246 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-io/2.0.2/plexus-io-2.0.2.pom
Progress (1): 1.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-io/2.0.2/plexus-io-2.0.2.pom (1.7 kB at 48 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.19/plexus-components-1.1.19.pom
Progress (1): 2.7 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-components/1.1.19/plexus-components-1.1.19.pom (2.7 kB at 73 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/3.0.1/plexus-3.0.1.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 12 kB Progress (1): 16 kBProgress (1): 19 kB                   Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus/3.0.1/plexus-3.0.1.pom (19 kB at 503 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.15/plexus-interpolation-1.15.pom
Progress (1): 1.0 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.15/plexus-interpolation-1.15.pom (1.0 kB at 34 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-lang/commons-lang/2.1/commons-lang-2.1.pom
Progress (1): 4.1 kBProgress (1): 8.2 kBProgress (1): 9.9 kB                    Downloaded from central: https://repo.maven.apache.org/maven2/commons-lang/commons-lang/2.1/commons-lang-2.1.pom (9.9 kB at 284 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-archiver/2.5/maven-archiver-2.5.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.15/plexus-interpolation-1.15.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-archiver/2.1/plexus-archiver-2.1.jar
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-io/2.0.2/plexus-io-2.0.2.jar
Progress (1): 4.1/38 kBProgress (1): 7.7/38 kBProgress (1): 11/38 kB Progress (1): 15/38 kBProgress (1): 20/38 kBProgress (1): 24/38 kBProgress (1): 28/38 kBProgress (1): 32/38 kBProgress (1): 36/38 kBProgress (1): 38 kB   Progress (2): 38 kB | 4.1/22 kBProgress (2): 38 kB | 7.7/22 kBProgress (2): 38 kB | 11/22 kB Progress (3): 38 kB | 11/22 kB | 4.1/184 kBProgress (3): 38 kB | 11/22 kB | 7.7/184 kBProgress (3): 38 kB | 15/22 kB | 7.7/184 kBProgress (3): 38 kB | 20/22 kB | 7.7/184 kBProgress (3): 38 kB | 22 kB | 7.7/184 kB   Progress (3): 38 kB | 22 kB | 12/184 kB Progress (3): 38 kB | 22 kB | 16/184 kBProgress (3): 38 kB | 22 kB | 20/184 kBProgress (3): 38 kB | 22 kB | 24/184 kBProgress (3): 38 kB | 22 kB | 28/184 kBProgress (3): 38 kB | 22 kB | 32/184 kBProgress (3): 38 kB | 22 kB | 36/184 kBProgress (3): 38 kB | 22 kB | 40/184 kBProgress (3): 38 kB | 22 kB | 45/184 kBProgress (3): 38 kB | 22 kB | 49/184 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 4.1/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 7.7/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 12/58 kB Progress (4): 38 kB | 22 kB | 49/184 kB | 16/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 20/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 24/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 28/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 32/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 36/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 40/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 45/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 49/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 53/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 57/58 kBProgress (4): 38 kB | 22 kB | 49/184 kB | 58 kB   Progress (4): 38 kB | 22 kB | 53/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 57/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 61/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 65/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 69/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 73/184 kB | 58 kBProgress (4): 38 kB | 22 kB | 77/184 kB | 58 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 4.1/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 8.2/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 12/60 kB Progress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 16/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 20/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 25/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 29/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 33/60 kBProgress (5): 38 kB | 22 kB | 77/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 81/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 86/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 90/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 94/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 98/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 102/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 106/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 110/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 114/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 118/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 122/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 126/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 131/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 135/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 139/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 143/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 147/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 151/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 37/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 41/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 45/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 49/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 53/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 57/60 kBProgress (5): 38 kB | 22 kB | 155/184 kB | 58 kB | 60 kB                                                           Downloaded from central: https://repo.maven.apache.org/maven2/classworlds/classworlds/1.1-alpha-2/classworlds-1.1-alpha-2.jar (38 kB at 695 kB/s)
Downloading from central: https://repo.maven.apache.org/maven2/commons-lang/commons-lang/2.1/commons-lang-2.1.jar
Progress (4): 22 kB | 159/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 163/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 167/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 172/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 176/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 180/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 184/184 kB | 58 kB | 60 kBProgress (4): 22 kB | 184 kB | 58 kB | 60 kB                                                Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-io/2.0.2/plexus-io-2.0.2.jar (58 kB at 1.1 MB/s)
Downloading from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0/plexus-utils-3.0.jar
Downloaded from central: https://repo.maven.apache.org/maven2/org/apache/maven/maven-archiver/2.5/maven-archiver-2.5.jar (22 kB at 370 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-interpolation/1.15/plexus-interpolation-1.15.jar (60 kB at 852 kB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-archiver/2.1/plexus-archiver-2.1.jar (184 kB at 2.6 MB/s)
Progress (1): 4.1/208 kBProgress (1): 7.7/208 kBProgress (1): 11/208 kB Progress (1): 15/208 kBProgress (1): 20/208 kBProgress (1): 24/208 kBProgress (1): 28/208 kBProgress (1): 32/208 kBProgress (1): 36/208 kBProgress (1): 40/208 kBProgress (1): 44/208 kBProgress (1): 48/208 kBProgress (1): 52/208 kBProgress (1): 56/208 kBProgress (1): 61/208 kBProgress (1): 65/208 kBProgress (1): 69/208 kBProgress (1): 73/208 kBProgress (1): 77/208 kBProgress (1): 81/208 kBProgress (1): 85/208 kBProgress (1): 89/208 kBProgress (1): 93/208 kBProgress (1): 97/208 kBProgress (2): 97/208 kB | 4.1/226 kBProgress (2): 97/208 kB | 7.7/226 kBProgress (2): 97/208 kB | 12/226 kB Progress (2): 97/208 kB | 16/226 kBProgress (2): 97/208 kB | 20/226 kBProgress (2): 97/208 kB | 24/226 kBProgress (2): 97/208 kB | 28/226 kBProgress (2): 97/208 kB | 32/226 kBProgress (2): 97/208 kB | 36/226 kBProgress (2): 97/208 kB | 40/226 kBProgress (2): 97/208 kB | 45/226 kBProgress (2): 97/208 kB | 49/226 kBProgress (2): 97/208 kB | 53/226 kBProgress (2): 97/208 kB | 57/226 kBProgress (2): 101/208 kB | 57/226 kBProgress (2): 106/208 kB | 57/226 kBProgress (2): 110/208 kB | 57/226 kBProgress (2): 114/208 kB | 57/226 kBProgress (2): 118/208 kB | 57/226 kBProgress (2): 122/208 kB | 57/226 kBProgress (2): 126/208 kB | 57/226 kBProgress (2): 130/208 kB | 57/226 kBProgress (2): 134/208 kB | 57/226 kBProgress (2): 138/208 kB | 57/226 kBProgress (2): 142/208 kB | 57/226 kBProgress (2): 147/208 kB | 57/226 kBProgress (2): 151/208 kB | 57/226 kBProgress (2): 155/208 kB | 57/226 kBProgress (2): 159/208 kB | 57/226 kBProgress (2): 163/208 kB | 57/226 kBProgress (2): 167/208 kB | 57/226 kBProgress (2): 171/208 kB | 57/226 kBProgress (2): 175/208 kB | 57/226 kBProgress (2): 175/208 kB | 61/226 kBProgress (2): 175/208 kB | 65/226 kBProgress (2): 175/208 kB | 69/226 kBProgress (2): 175/208 kB | 73/226 kBProgress (2): 175/208 kB | 77/226 kBProgress (2): 175/208 kB | 81/226 kBProgress (2): 175/208 kB | 86/226 kBProgress (2): 175/208 kB | 90/226 kBProgress (2): 175/208 kB | 94/226 kBProgress (2): 175/208 kB | 98/226 kBProgress (2): 175/208 kB | 102/226 kBProgress (2): 175/208 kB | 106/226 kBProgress (2): 175/208 kB | 110/226 kBProgress (2): 175/208 kB | 114/226 kBProgress (2): 175/208 kB | 118/226 kBProgress (2): 175/208 kB | 122/226 kBProgress (2): 175/208 kB | 126/226 kBProgress (2): 175/208 kB | 131/226 kBProgress (2): 175/208 kB | 135/226 kBProgress (2): 175/208 kB | 139/226 kBProgress (2): 175/208 kB | 143/226 kBProgress (2): 175/208 kB | 147/226 kBProgress (2): 175/208 kB | 151/226 kBProgress (2): 175/208 kB | 155/226 kBProgress (2): 175/208 kB | 159/226 kBProgress (2): 175/208 kB | 163/226 kBProgress (2): 175/208 kB | 167/226 kBProgress (2): 175/208 kB | 172/226 kBProgress (2): 175/208 kB | 176/226 kBProgress (2): 175/208 kB | 180/226 kBProgress (2): 175/208 kB | 184/226 kBProgress (2): 175/208 kB | 188/226 kBProgress (2): 175/208 kB | 192/226 kBProgress (2): 175/208 kB | 196/226 kBProgress (2): 175/208 kB | 200/226 kBProgress (2): 175/208 kB | 204/226 kBProgress (2): 175/208 kB | 208/226 kBProgress (2): 175/208 kB | 213/226 kBProgress (2): 175/208 kB | 217/226 kBProgress (2): 175/208 kB | 221/226 kBProgress (2): 175/208 kB | 225/226 kBProgress (2): 175/208 kB | 226 kB    Progress (2): 179/208 kB | 226 kBProgress (2): 183/208 kB | 226 kBProgress (2): 187/208 kB | 226 kBProgress (2): 192/208 kB | 226 kBProgress (2): 196/208 kB | 226 kBProgress (2): 200/208 kB | 226 kBProgress (2): 204/208 kB | 226 kBProgress (2): 208 kB | 226 kB                                 Downloaded from central: https://repo.maven.apache.org/maven2/org/codehaus/plexus/plexus-utils/3.0/plexus-utils-3.0.jar (226 kB at 2.3 MB/s)
Downloaded from central: https://repo.maven.apache.org/maven2/commons-lang/commons-lang/2.1/commons-lang-2.1.jar (208 kB at 2.0 MB/s)
[[1;34mINFO[m] Building jar: /root/exp_SWAT/java-ai-assistant/target/java-ai-assistant-1.0.0-SNAPSHOT.jar
[[1;34mINFO[m] 
[[1;34mINFO[m] [1m--- [0;32mjacoco-maven-plugin:0.8.13:report[m [1m(report)[m @ [36mjava-ai-assistant[0;1m ---[m
[[1;34mINFO[m] Loading execution data file /root/exp_SWAT/java-ai-assistant/target/jacoco.exec
[[1;34mINFO[m] Analyzed bundle 'java-ai-assistant' with 3 classes
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] [1;32mBUILD SUCCESS[m
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m
[[1;34mINFO[m] Total time:  22.456 s
[[1;34mINFO[m] Finished at: 2026-06-11T08:09:45Z
[[1;34mINFO[m] [1m------------------------------------------------------------------------[m

[MAVEN_RETRY_EXIT_CODE=0]
```

## 提交与推送日志

```text
COMMIT: bae2482
BRANCH: 202606110715_java_ai_assistant_full_dev
remote: 
remote: Create a pull request for '202606110715_java_ai_assistant_full_dev' on GitHub by visiting:        
remote:      https://github.com/ChuXunYu/exp_SWAT/pull/new/202606110715_java_ai_assistant_full_dev        
remote: 
To https://github.com/ChuXunYu/exp_SWAT.git
 * [new branch]      202606110715_java_ai_assistant_full_dev -> 202606110715_java_ai_assistant_full_dev
branch '202606110715_java_ai_assistant_full_dev' set up to track 'origin/202606110715_java_ai_assistant_full_dev'.

[PUSH_EXIT_CODE=0]
```
