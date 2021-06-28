![Banner](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/misc/banner.jpg)  

[![License Apache 2.0](https://img.shields.io/badge/license-Apache%20License%202.0-green.svg)](http://www.apache.org/licenses/LICENSE-2.0)
[![Java 8.0+](https://img.shields.io/badge/java-8.0%2b-green.svg)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
[![NetBeans](https://img.shields.io/badge/netbeans-9.0%2b-green.svg)](http://plugins.netbeans.org/plugin/60188/nb-mindmap-editor)
[![Intellij IDEA](https://img.shields.io/badge/idea-14.0.5%2b-green.svg)](https://plugins.jetbrains.com/plugin/8045)
[![PayPal donation](https://img.shields.io/badge/donation-PayPal-cyan.svg)](https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=AHWJHJFBAWGL2)
[![YooMoney donation](https://img.shields.io/badge/donation-Yoo.money-blue.svg)](https://yoomoney.ru/to/41001158080699)

# Introduction
The Main part of a software project is not code but knowledge generated during the project. I guess knowledge is the main part of every business today and it is very important to move the generated knowledge from non-formal level to formal level to avoid its loss. There are many approaches to save knowledge and [a mind map diagram](https://en.wikipedia.org/wiki/Mind_map) is one of them. Also it is very important to keep the "distance" between project and its knowledge as short as possible and the ideal variant is to keep knowledge just within the project as a document set. If you use any external knowledge engineering tool then the distance already is not so short and for the cause I had decided to develop some plugin which would allow to form mind maps and keep knowledge graph just within projects and allow to work with the graphs just with the IDE.   

Initially I developed plugin for NetBeans IDE but because in companies, where I work for, mainly Intellij IDEA in use, I developed plugin for Intellij IDEA too. Then a friend asked me to develop some standalone version which could be used by users who don't have any relations to Java and I developed SciaReto application.   
![Screenshots from all](./assets/screenshots_all.png)

# Changelog
__1.4.12 (SNAPSHOT)__
- SR: RSyntaxTextPanel updated to 3.1.3, Kotlin syntax support added
- IJ: fixed incompatibility with Intellij IDEA 2021.2 EAP
- IJ: __minimal Intellij IDEA platform version lifted up to 2018.1__
- SR: updated PlantUML version to 1.2021.8
- SR: improved processing of MMD files during "Find files for text" [#83](https://github.com/raydac/netbeans-mmd-plugin/issues/83) 
- SR: Updated Launch4j starter

__1.4.11 (28-mar-2021)__
- NB:  __stopped support NetBeans 8.0, minimal supported version 9.0__
- NB:  NetBeans plugin published in Maven central
- SR:  improved undo for RSyntax panel [#80](https://github.com/raydac/netbeans-mmd-plugin/issues/80) 
- IJ:  fixed resource bundle loading under Intellij IDEA 2021.1 EAP
- ALL: improved text representation of mind map for clipboard
- ALL: improved folder processing in import-export plugins
- SR:  embedded JDK version updated to 11.0.10+9
- SR:  updated RSyntax panel version to 3.1.2
- SR:  updated PlantUML version to 1.2021.3

__1.4.10 (14-dec-2020)__
- SR:  added View-Zoom into the main menu toolbar
- ALL: added password protection for notes (AES-256)
- ALL: decreased topic dragging start sensitivity [#77](https://github.com/raydac/netbeans-mmd-plugin/issues/77)
- ALL: emoticons and images included into search scope by their names [#76](https://github.com/raydac/netbeans-mmd-plugin/issues/76)
- SR:  fixed file rename [#74](https://github.com/raydac/netbeans-mmd-plugin/issues/74)
- SR:  embedded JDK version updated to 11.0.9.1+1
- SR:  embedded PlantUml updated to 1.2020.22

[Full changelog](https://github.com/raydac/netbeans-mmd-plugin/blob/master/changelog.txt)   

# Implementation

All parts of the application are written in Java and it needs Java 1.8+ for work.   

The editor has three implementations
- standalone application (including also [PlantUML](http://plantuml.com/) support)
- [NetBeans plug-in](http://plugins.netbeans.org/plugin/60188/nb-mindmap-editor)
- [Intellij IDEA plug-in](https://plugins.jetbrains.com/plugin/8045-idea-mind-map)   
The standalone version is a Java application but it contains embedded JRE so that a user should not have pre-installed Java on computer. But [Graphviz](https://www.graphviz.org/download/) should be installed for use by PlantUML.

# How to use?

Just after first release for NetBeans IDE, [Geertjan Wielenga created nice screen-cast](https://www.youtube.com/watch?v=7TUU25dsOfM) which a bit out of date but still look good to understand common idea. It is very easy editor and the main set of operations look very intuitive, main questions from users I had about keyboard shortcuts but if keep in mind that __TAB__ creates new mind map node and __ENTER__ creates new sibling mode then life becomes much easier. It is a pop-up menu centric software so that all operations accessible through pop-up menu.

# Key features
## Data format
The Plugin keeps all mind map information in single UTF8 encoded plain text file formed in markdown compatible format, so the file can be easily read and interpret and without the plug-in.  

```
Mind Map generated by NB MindMap plugin   
> __version__=`1.1`
---

# Root <br/>topic
> topicLinkUID=`14F9D4CD31DA`


## First level
- LINK
<pre>http://www.google.com</pre>

### Item 1\.1
- NOTE
<pre>Some note for item 1.1</pre>

### Item 1\.2
- TOPIC
<pre>14F9D4CD31DA</pre>
```

## How to create new Mind map
### SciaReto standalone application
In project tree just click mouse to activate pop-up menu for a folder and select in menu __New->Mind map__.
![New mind map file](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/newfiledialog_sciareto.png)  

### NetBeans
Mind map type is added as one more file type and can be found among file types in the __Other__.   
![New file wizard](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/newfiledialog.png)  

### Intellij IDEA
Mind map type is registered as a file type which can be created through __New__ pop-up menu for a folder.
![New MindMap file](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/newfiledialog_idea.png)  

## Work with mind maps
Mind maps are opened in IDE as documents with graphic interface and the plugin doesn't add any new actions into IDE menu so all operations over mind maps you can make through the document pop-up menu. The Pop-up menu is formed for the current document state and context.  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/popup.png)  

## Move topics
All manipulations over topic order and its position in the map hierarchy can be made only through mouse Drag&Drop operations (keep in mind that only one topic can be moved in the same time).  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/dragdroptopic.png)  

## Tune look of mind map editor
You can tune most of graphic parameters for mind map rendering through special panel __Options__. It can be activated through __Options__ item of pop-up menu and through menu __Edit->Preferences__ in the standalone version.  
![Screenshot](https://raw.githubusercontent.com/raydac/netbeans-mmd-plugin/master/assets/optionspanel.png)  

## Export mind map data
Today it is very important to have way to export data to another formats to use the knowledge in another tools. The Plug-in supports several formats to export data:  

- __Plain text__ format
- __Markdown__ format
- __Freemind__ format
- __Mindmup__ format
- __PNG__ image
- __SVG__ image
- __ORG__ format

## Import data as Mind map
At present the editor allows to import main maps from many well-known mind map formats
- __Mindmup__ format
- __Freemind__ format
- __XMind__ format
- __Coggle__ format
- __Novamind__ format
also it can import mind map from tabbed text files.

## Content alowed in mind maps
Mainly you can keep short texts in your mind maps as topic names but sometime it is useful to keep and another information, the plugin allows to keep below listed information assets:

-  __Short text__ - short text in one or two lines as topic name, mainly used as id for the topic.
-  __Long text__ - long text information which can be opened and edited in special editor.
-  __URL link__ - just URL link to be opened in system browser
-  __File link__ - link to a file, youc can add them through special dialog or just drag and drop file from NetBeans project tree to a topic, **the file will be opened within IDE as a document**
-  __Transition__ - link to another topic in the same mind map, it allows to make some navigation in very big mind maps

You can add and edit new topic content through the mind map pop-up menu and if you want remove some content then just open editor for the content and remove all text information.
