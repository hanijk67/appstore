
    alter table PF1HANDLERAPP
        add PENVIRONMENT number(10,0);

    alter table PF1HANDLERAPP
        add PORGID number(19,0) ;


    alter table PF1HANDLERAPP
        add PUPLOADEDTESTFILEDDT number(10,0) ;

    alter table PF1HANDLERAPP
        add PUPLOADEDTESTFILEDTM number(10,0) ;

    alter table PF1HANDLERAPP
        add PTESTFILEID number(19,0) ;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2690420FD
        foreign key (PTESTFILEID)
        references PC1FILE
 ;
    create table PF1ORG (
        PORGID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PENGLISHFULLNAME varchar2(255 char),
        PFULLNAME varchar2(255 char),
        PNICKNAME varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        PICONFILEID number(19,0),
        primary key (PORGID)
    );

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2BDEAB22A
        foreign key (PORGID)
        references PF1ORG ;

    alter table PF1ORG
        add constraint FK8C7597E91C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER ;

    alter table PF1ORG
        add constraint FK8C7597E9FB81C05F
        foreign key (CREATOR)
        references PC1USER ;

    alter table PF1ORG
        add constraint FK8C7597E9DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER ;

    alter table PF1ORG
        add constraint FK8C7597E9D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER ;

    alter table PF1ORG
        add constraint FK8C7597E9E4694FA4
        foreign key (PICONFILEID)
        references PC1FILE ;

    create sequence SEQ_ORG start with 1000 increment by 1;

    create table PC1OSENVIRONMENT (
        PENVID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PENVNAME varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PENVID)
    )
;
    alter table PF1HANDLERAPP
        add PENVID number(19,0)
;
    create table mm_anouncement_environment (
        PANOUNCEMENTID number(19,0) not null,
        PENVID number(19,0) not null
    )
;
    create table mm_anouncement_organinzation (
        PANOUNCEMENTID number(19,0) not null,
        PPORGID number(19,0) not null
    )
;
    alter table PC1OSENVIRONMENT
        add constraint FKD1B221B11C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PC1OSENVIRONMENT
        add constraint FKD1B221B1FB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PC1OSENVIRONMENT
        add constraint FKD1B221B1DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PC1OSENVIRONMENT
        add constraint FKD1B221B1D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2A10D3E9F
        foreign key (PENVID)
        references PC1OSENVIRONMENT
;
    alter table mm_anouncement_environment
        add constraint FK800272B0A7F49779
        foreign key (PANOUNCEMENTID)
        references PC1ANOUNCEMENT
;
    alter table mm_anouncement_environment
        add constraint FK800272B0A10D3E9F
        foreign key (PENVID)
        references PC1OSENVIRONMENT
;
    alter table mm_anouncement_organinzation
        add constraint FKF303D28EE2A49E9A
        foreign key (PPORGID)
        references PF1ORG
;
    alter table mm_anouncement_organinzation
        add constraint FKF303D28EA7F49779
        foreign key (PANOUNCEMENTID)
        references PC1ANOUNCEMENT
;
    create sequence SEQ_ENV start with 1000 increment by 1;

  alter table PF1ORG
        add PISDEFAULT number(1,0)
        ;

alter table PF1OS
        add POSCODE varchar2(255 char);

        ALTER TABLE PF1OS
ADD CONSTRAINT constraint_name UNIQUE (POSCODE);

    alter table PF1HANDLERAPP
        add PISDEFAULTFORORG number(1,0);


 create table PC1FLAGFORINSERT (
        PAPPID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PISLAUNCHED number(1,0),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PAPPID)
    )
;
    alter table PC1FLAGFORINSERT
        add constraint FK586C42D81C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PC1FLAGFORINSERT
        add constraint FK586C42D8FB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PC1FLAGFORINSERT
        add constraint FK586C42D8DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PC1FLAGFORINSERT
        add constraint FK586C42D8D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    create sequence SEQ_FLAG_INS start with 1000 increment by 1 ;


 alter table PC1APP
        add PISDELETED number(1,0);

alter table PC1PACKAGE
        add PISDELETED number(1,0);


set serveroutput on;
declare
handlerConstName varchar2(30);
appConstName varchar2(30);
 begin
select CONSTRAINT_NAME into handlerConstName
from USER_CONSTRAINTS
where TABLE_NAME = UPPER('PF1HANDLERAPP') and CONSTRAINT_TYPE ='U' ;

execute immediate 'alter table PF1HANDLERAPP DROP CONSTRAINT ' ||  handlerConstName ;
execute immediate 'ALTER TABLE PF1HANDLERAPP ADD CONSTRAINT '|| handlerConstName ||' UNIQUE (PVERSIONCODE, POSID, PORGID, PENVID)';

select CONSTRAINT_NAME into appConstName
from USER_CONSTRAINTS
where TABLE_NAME = UPPER('PC1APP') and CONSTRAINT_TYPE ='U' ;
execute immediate 'alter table PC1APP DROP CONSTRAINT ' ||  appConstName ;
end;
