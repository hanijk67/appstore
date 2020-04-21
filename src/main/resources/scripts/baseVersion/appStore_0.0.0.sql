create table PC1APP (PAPPID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PAPPDESC clob, PPACKNAME varchar2(255 char), PTITLE varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), PAPPCATID number(19,0), PDEVELOPER number(19,0), PMAINPACKID number(19,0), P_OS number(19,0), POSTYP number(19,0), primary key (PAPPID), unique (PPACKNAME, POSTYP));
create table PC1COMMENT (PCOMMENTID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PISAPPROVED number(1,0), PTEXT varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PCOMMENTID));
create table PC1DEVICE (PDEVICEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PACTIVE number(1,0), PDEV_STATE number(3,0), PIMEI varchar2(255 char), PTITLE varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), PIMAGEFILEID number(19,0), P_OSID number(19,0), P_OSTYPEID number(19,0), PUSED_BYID number(19,0), primary key (PDEVICEID));
create table PC1FILE (PFILEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PFILENAME varchar2(255 char), PFILEPATH varchar2(255 char), folder varchar2(255 char), type number(10,0) not null, CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PFILEID));
create table PC1NOTE (PNOTEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PNOTETXT varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PNOTEID));
create table PC1PACKAGE (PAPPPACKID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PCERTINFO clob, PCHANGELOG clob, PMINSDK varchar2(255 char), PPUB_STATE number(3,0), PTARGETSDK varchar2(255 char), PVERSIONCODE varchar2(255 char), PVERSIONNAME varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), PICONFILEID number(19,0), PFILEID number(19,0), primary key (PAPPPACKID));
create table PC1RATE (PRATEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PRATEVAL number(10,0), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PRATEID));
create table PC1USER (PUSERID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PADRS varchar2(255 char), PEMAIL varchar2(255 char), PFIRSTNAME varchar2(255 char), PLASTNAME varchar2(255 char), PMOBAREACOD varchar2(255 char), PMOBNUM varchar2(255 char), nationalCode varchar2(255 char), PTELAREACOD varchar2(255 char), PTELNUM varchar2(255 char), PGENDER number(3,0), PLASTIP varchar2(255 char), PLAST_LOGIN_DATE number(10,0), PLAST_LOGIN_TIME number(10,0), PLOGGED number(1,0), PNUMOFWRONGTRIES number(3,0), PPASSWD varchar2(255 char), PPASSSALT varchar2(255 char), PPRIVKEY clob, PPUBKEY clob, PUSEUSERID number(19,0) unique, PUSERNAME varchar2(255 char) unique, status number(3,0) not null, CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), PCITYID number(19,0), primary key (PUSERID));
create table PF1CATEGORY (PAPPCATID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PAPPCATNAM varchar2(255 char), PISASSIGNABLE number(1,0), PISENABLED number(1,0), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), PPARENT number(19,0), primary key (PAPPCATID));
create table PF1COUNTRY (PCOUNTRYID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PNAME varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PCOUNTRYID));
create table PF1OS (POSID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PDISABLED number(1,0), PHANDLERAPPDOWNLOADPATH varchar2(255 char), POSNAME varchar2(255 char), POSVER varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), POSTYP number(19,0), primary key (POSID));
create table PF1OSTYP (POSTYPID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PDISABLED number(1,0), PNAME varchar2(255 char), POSCOMPSCRIPT clob, CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (POSTYPID));
create table PF1ROLE (PROLEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PACCESSCODS varchar2(255 char), Peditable number(1,0), PROLENAME varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), primary key (PROLEID));
create table PF1STATE (PSTATEID number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), PNAME varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), country number(19,0), primary key (PSTATEID));
create table TBL_CITY (id number(19,0) not null, PCREATIONDT number(10,0), PCREATIONTM number(10,0), PHBCREATIONDT number(10,0), PHBCREATIONTM number(10,0), PHBLASTMDFDT number(10,0), PHBLASTMDFTM number(10,0), PLASTMDFDT number(10,0), PLASTMDFTM number(10,0), PHBVER number(19,0), name varchar2(255 char), CREATOR number(19,0), PHBCREATOR number(19,0), PHBLASTMDFUSR number(19,0), LASTMDFUSER number(19,0), state number(19,0), primary key (id));
create table app2ThumbFiles (PAPPPACKID number(19,0) not null, PFILEID number(19,0) not null);
create table app2apppack (PAPPID number(19,0) not null, PAPPPACKID number(19,0) not null, unique (PAPPPACKID));
create table app2comment (PAPPID number(19,0) not null, PCOMMENTID number(19,0) not null, unique (PCOMMENTID));
create table app2rate (PAPPID number(19,0) not null, PRATEID number(19,0) not null, unique (PRATEID));
create table mm_user_roles (PC1USER_PUSERID number(19,0) not null, roles_PROLEID number(19,0) not null, primary key (PC1USER_PUSERID, roles_PROLEID));
alter table PC1APP add constraint FK8C4B1CA31C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1APP add constraint FK8C4B1CA3FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1APP add constraint FK8C4B1CA3DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1APP add constraint FK8C4B1CA3DD89D9AA foreign key (PMAINPACKID) references PC1PACKAGE;
alter table PC1APP add constraint FK8C4B1CA3FA8AB83F foreign key (P_OS) references PF1OS;
alter table PC1APP add constraint FK8C4B1CA3B7CD693D foreign key (POSTYP) references PF1OSTYP;
alter table PC1APP add constraint FK8C4B1CA39985A58D foreign key (PDEVELOPER) references PC1USER;
alter table PC1APP add constraint FK8C4B1CA3D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1APP add constraint FK8C4B1CA3380A8337 foreign key (PAPPCATID) references PF1CATEGORY;
alter table PC1COMMENT add constraint FK6A6777E11C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1COMMENT add constraint FK6A6777E1FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1COMMENT add constraint FK6A6777E1DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1COMMENT add constraint FK6A6777E1D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D8741C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D874FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D874DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D874BEE40C9A foreign key (P_OSID) references PF1OS;
alter table PC1DEVICE add constraint FK1D60D8749AA5B8B7 foreign key (PUSED_BYID) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D874D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1DEVICE add constraint FK1D60D874BD5C3026 foreign key (PIMAGEFILEID) references PC1FILE;
alter table PC1DEVICE add constraint FK1D60D87472C1E12E foreign key (P_OSTYPEID) references PF1OSTYP;
alter table PC1FILE add constraint FKFD1AA31A1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1FILE add constraint FKFD1AA31AFB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1FILE add constraint FKFD1AA31ADBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1FILE add constraint FKFD1AA31AD6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1NOTE add constraint FKFD1E5D901C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1NOTE add constraint FKFD1E5D90FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1NOTE add constraint FKFD1E5D90DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1NOTE add constraint FKFD1E5D90D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1PACKAGE add constraint FK1A6BDC81C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1PACKAGE add constraint FK1A6BDC8FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1PACKAGE add constraint FK1A6BDC8DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1PACKAGE add constraint FK1A6BDC896EEC9EB foreign key (PFILEID) references PC1FILE;
alter table PC1PACKAGE add constraint FK1A6BDC8D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1PACKAGE add constraint FK1A6BDC8E4694FA4 foreign key (PICONFILEID) references PC1FILE;
alter table PC1RATE add constraint FKFD1FFA7E1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1RATE add constraint FKFD1FFA7EFB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1RATE add constraint FKFD1FFA7EDBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1RATE add constraint FKFD1FFA7ED6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PC1USER add constraint FKFD2199691C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PC1USER add constraint FKFD21996991D2D209 foreign key (PCITYID) references TBL_CITY;
alter table PC1USER add constraint FKFD219969FB81C05F foreign key (CREATOR) references PC1USER;
alter table PC1USER add constraint FKFD219969DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PC1USER add constraint FKFD219969D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1CATEGORY add constraint FKE64EB6F91C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1CATEGORY add constraint FKE64EB6F9FB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1CATEGORY add constraint FKE64EB6F9DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1CATEGORY add constraint FKE64EB6F9D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1CATEGORY add constraint FKE64EB6F9FDB345F1 foreign key (PPARENT) references PF1CATEGORY;
alter table PF1COUNTRY add constraint FK27A62E1B1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1COUNTRY add constraint FK27A62E1BFB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1COUNTRY add constraint FK27A62E1BDBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1COUNTRY add constraint FK27A62E1BD6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1OS add constraint FK487EC1F1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1OS add constraint FK487EC1FFB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1OS add constraint FK487EC1FDBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1OS add constraint FK487EC1FB7CD693D foreign key (POSTYP) references PF1OSTYP;
alter table PF1OS add constraint FK487EC1FD6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1OSTYP add constraint FK456FF1EC1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1OSTYP add constraint FK456FF1ECFB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1OSTYP add constraint FK456FF1ECDBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1OSTYP add constraint FK456FF1ECD6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1ROLE add constraint FK23EB7F11C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1ROLE add constraint FK23EB7F1FB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1ROLE add constraint FK23EB7F1DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1ROLE add constraint FK23EB7F1D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table PF1STATE add constraint FK45A87C561C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table PF1STATE add constraint state_country_fk foreign key (country) references PF1COUNTRY;
alter table PF1STATE add constraint FK45A87C56FB81C05F foreign key (CREATOR) references PC1USER;
alter table PF1STATE add constraint FK45A87C56DBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table PF1STATE add constraint FK45A87C56D6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table TBL_CITY add constraint FK4048996C1C8DAD7 foreign key (LASTMDFUSER) references PC1USER;
alter table TBL_CITY add constraint FK4048996CFB81C05F foreign key (CREATOR) references PC1USER;
alter table TBL_CITY add constraint FK4048996CDBA383B5 foreign key (PHBCREATOR) references PC1USER;
alter table TBL_CITY add constraint FK4048996CD6530CB8 foreign key (PHBLASTMDFUSR) references PC1USER;
alter table TBL_CITY add constraint city_state_fk foreign key (state) references PF1STATE;
alter table app2ThumbFiles add constraint FK6FDD2B52D74CA932 foreign key (PAPPPACKID) references PC1PACKAGE;
alter table app2ThumbFiles add constraint FK6FDD2B5296EEC9EB foreign key (PFILEID) references PC1FILE;
alter table app2apppack add constraint FK436E7069D74CA932 foreign key (PAPPPACKID) references PC1PACKAGE;
alter table app2apppack add constraint FK436E7069151F4145 foreign key (PAPPID) references PC1APP;
alter table app2comment add constraint FKAB5A7B0E151F4145 foreign key (PAPPID) references PC1APP;
alter table app2comment add constraint FKAB5A7B0E968DBE41 foreign key (PCOMMENTID) references PC1COMMENT;
alter table app2rate add constraint FK43209691AB012FB3 foreign key (PRATEID) references PC1RATE;
alter table app2rate add constraint FK43209691151F4145 foreign key (PAPPID) references PC1APP;
alter table mm_user_roles add constraint FK888E1D4890E531DD foreign key (roles_PROLEID) references PF1ROLE;
alter table mm_user_roles add constraint FK888E1D485BDFA9F3 foreign key (PC1USER_PUSERID) references PC1USER;

create sequence SEQ_APP start with 1000 increment by 1;
create sequence SEQ_APP_CAT start with 1000 increment by 1;
create sequence SEQ_APP_PACK start with 1000 increment by 1;
create sequence SEQ_COMMENT start with 1000 increment by 1;
create sequence SEQ_DEVICE start with 1000 increment by 1;
create sequence SEQ_FILE start with 1000 increment by 1;
create sequence SEQ_NOTE start with 1000 increment by 1;
create sequence SEQ_OS start with 1000 increment by 1;
create sequence SEQ_OSTYP start with 1000 increment by 1;
create sequence SEQ_RATE start with 1000 increment by 1;
create sequence SEQ_ROLE start with 1000 increment by 1;
create sequence SEQ_USER start with 1000 increment by 1;


    create table PC1ANOUNCEMENT (
        PANOUNCEMENTID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PACTIONCATEGORY varchar2(255 char),
        PACTIONDESCRIPTOR clob,
        PANOUNCMENTFILEKEY varchar2(255 char),
        PANOUNCEMENTTEXT varchar2(255 char),
        PANOUNCE_TYPE number(10,0),
        PEXPIREDT number(10,0),
        PEXPIRETM number(10,0),
        PISACTIVE number(1,0),
        PSTARTDT number(10,0),
        PSTARTTM number(10,0),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PANOUNCEMENTID)
    );

    create table PC1TESTSUBISSUE (
        PTSTSUBISSUEID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPROVAL_STATE number(3,0),
        PDESCRIPTION varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        PTESTUSER number(19,0),
        primary key (PTSTSUBISSUEID)
    );

    create table PC1TESTISSUE (
        PTESTISSUEID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPROVAL_STATE number(3,0),
        PDESCRIPTION clob,
        PPRIORITY_STATE number(3,0),
        PTITLE varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PTESTISSUEID)
    );

    create table PC1TESTGROUP (
        PTESTGROUPID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PTITLE varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PTESTGROUPID)
    );

    alter table PC1APP
        add PMAINPACKMODDT number(10,0);

    alter table PC1APP
        add PMAINPACKMODTM number(10,0);

    alter table PC1APP
        add PRELATEDCALCDT number(10,0);

    alter table PC1APP
        add PRELATEDCALCTM number(10,0);

    alter table PC1APP
        add PSHORTDESCRIOTION varchar2(255 char);

    create table PC1APPHISTORY (
        PAPPHISTORYID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPDESC clob,
        PPACKNAME varchar2(255 char),
        PTITLE varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        PAPPCATID number(19,0),
        PDEVELOPER number(19,0),
        PMAINPACKID number(19,0),
        P_OS number(19,0),
        POSTYP number(19,0),
        primary key (PAPPHISTORYID)
    );

    create table PC1APPINSTALLREPORTQUEUE (
        PAPPINSTALLQUEUEID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPINSTALLJSONSTRING clob,
        PDEVICEID varchar2(255 char),
        PISDELETED number(1,0),
        PSSOUSERID number(19,0),
        PSSOUSERNAME varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        POSTYP number(19,0),
        primary key (PAPPINSTALLQUEUEID)
    );

    create table package2group (
        PAPPPACKID number(19,0) not null,
        PTESTGROUPID number(19,0) not null,
        unique (PTESTGROUPID)
    );

    alter table package2group
        add constraint FK2655AD53D74CA932
        foreign key (PAPPPACKID)
        references PC1PACKAGE;

    alter table PC1FILE
        add PFILESIZE number(19,0);

    alter table PC1PACKAGE
        add PLASTPUBLISHDDT number(10,0);

    alter table PC1PACKAGE
        add PLASTPUBLISHDTM number(10,0);

    alter table PC1PACKAGE
        add PPERMDETAIL clob;

    create table PC1PACKAGEHISTORY (
        PAPPPACKHISTORYID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PCERTINFO clob,
        PCHANGELOG clob,
        PHASTESTCHANGE number(19,0),
        PHASTESTISSUECHANGE number(1,0),
        PMINSDK varchar2(255 char),
        PPUB_STATE number(3,0),
        PTARGETSDK varchar2(255 char),
        PVERSIONCODE varchar2(255 char),
        PVERSIONNAME varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        PICONFILEID number(19,0),
        PFILEID number(19,0),
        primary key (PAPPPACKHISTORYID)
    );

    create table PC1TESTISSUEHISTORY (
        PTESTISSUEHISTORYID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPROVAL_STATE number(3,0),
        PDESCRIPTION clob,
        PPRIORITY_STATE number(3,0),
        PTITLE varchar2(255 char),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PTESTISSUEHISTORYID)
    );

    alter table PF1CATEGORY
        add PICONFILEID number(19,0);

    create table PF1HANDLERAPP (
        PHANDLERAPPID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PISACTIVE number(1,0),
        PUPLOADEDFILEDDT number(10,0),
        PUPLOADEDFILEDTM number(10,0),
        PVERSIONCODE number(19,0),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        PFILEID number(19,0),
        POSID number(19,0),
        primary key (PHANDLERAPPID),
        unique (PVERSIONCODE, POSID)
    );

    create table app2history (
        PAPPID number(19,0) not null,
        PAPPHISTORYID number(19,0) not null,
        unique (PAPPHISTORYID)
    );

    create table mm_anouncement_osType (
        PANOUNCEMENTID number(19,0) not null,
        POSTYPID number(19,0) not null
    );

    create table mm_appHistory_apppack (
        PAPPHISTORYID number(19,0) not null,
        PAPPPACKID number(19,0) not null
    );

    create table mm_testIssueHist_subIssue (
        PTESTISSUEHISTORYID number(19,0) not null,
        PTSTSUBISSUEID number(19,0) not null
    );

    create table package2history (
        PAPPPACKID number(19,0) not null,
        PAPPPACKHISTORYID number(19,0) not null,
        unique (PAPPPACKHISTORYID)
    );

    create table MM_PACKHIST_GROUP (
        PAPPPACKHISTORYID number(19,0) not null,
        PTESTGROUPID number(19,0) not null
    );

    create table MM_PACKHIST_TESTISSUE (
        PAPPPACKHISTORYID number(19,0) not null,
        PTESTISSUEID number(19,0) not null
    );

    create table issue2subIssue (
        PTESTISSUEID number(19,0) not null,
        PTSTSUBISSUEID number(19,0) not null,
        unique (PTSTSUBISSUEID)
    );

    create table mm_pack2TestIssue (
        PAPPPACKID number(19,0) not null,
        PTESTISSUEID number(19,0) not null
    );

    create table testIssue2history (
        PTESTISSUEID number(19,0) not null,
        PTESTISSUEHISTORYID number(19,0) not null,
        unique (PTESTISSUEHISTORYID)
    );

    alter table MM_PACKHIST_GROUP
        add constraint FKE75A165A54FC863D
        foreign key (PTESTGROUPID)
        references PC1TESTGROUP;

    alter table MM_PACKHIST_GROUP
        add constraint FKE75A165A2918852C
        foreign key (PAPPPACKHISTORYID)
        references PC1PACKAGEHISTORY;


    alter table MM_PACKHIST_TESTISSUE
        add constraint FKF80621E2C0D2BC71
        foreign key (PTESTISSUEID)
        references PC1TESTISSUE;

    alter table MM_PACKHIST_TESTISSUE
        add constraint FKF80621E22918852C
        foreign key (PAPPPACKHISTORYID)
        references PC1PACKAGEHISTORY;

    alter table PC1ANOUNCEMENT
        add constraint FK8A5980FD1C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PC1ANOUNCEMENT
        add constraint FK8A5980FDFB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PC1ANOUNCEMENT
        add constraint FK8A5980FDDBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PC1ANOUNCEMENT
        add constraint FK8A5980FDD6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A866711C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A86671FB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A86671DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A86671DD89D9AA
        foreign key (PMAINPACKID)
        references PC1PACKAGE;

    alter table PC1APPHISTORY
        add constraint FK40A86671FA8AB83F
        foreign key (P_OS)
        references PF1OS;

    alter table PC1APPHISTORY
        add constraint FK40A86671B7CD693D
        foreign key (POSTYP)
        references PF1OSTYP;

    alter table PC1APPHISTORY
        add constraint FK40A866719985A58D
        foreign key (PDEVELOPER)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A86671D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table PC1APPHISTORY
        add constraint FK40A86671380A8337
        foreign key (PAPPCATID)
        references PF1CATEGORY;

    alter table PC1APPINSTALLREPORTQUEUE
        add constraint FKE8EFF5C51C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PC1APPINSTALLREPORTQUEUE
        add constraint FKE8EFF5C5FB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PC1APPINSTALLREPORTQUEUE
        add constraint FKE8EFF5C5DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PC1APPINSTALLREPORTQUEUE
        add constraint FKE8EFF5C5B7CD693D
        foreign key (POSTYP)
        references PF1OSTYP;

    alter table PC1APPINSTALLREPORTQUEUE
        add constraint FKE8EFF5C5D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121AC1C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121ACFB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121ACDBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121AC96EEC9EB
        foreign key (PFILEID)
        references PC1FILE;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121ACD6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table PC1PACKAGEHISTORY
        add constraint FKBFB121ACE4694FA4
        foreign key (PICONFILEID)
        references PC1FILE;

    alter table PC1TESTISSUEHISTORY
        add constraint FK29EA57CB1C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PC1TESTISSUEHISTORY
        add constraint FK29EA57CBFB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PC1TESTISSUEHISTORY
        add constraint FK29EA57CBDBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PC1TESTISSUEHISTORY
        add constraint FK29EA57CBD6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table PF1CATEGORY
        add constraint FKE64EB6F9E4694FA4
        foreign key (PICONFILEID)
        references PC1FILE;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D21C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2B4D69B
        foreign key (POSID)
        references PF1OS;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2FB81C05F
        foreign key (CREATOR)
        references PC1USER;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D296EEC9EB
        foreign key (PFILEID)
        references PC1FILE;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D2D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER;

    alter table app2history
        add constraint FKA9F47143836141B9
        foreign key (PAPPHISTORYID)
        references PC1APPHISTORY;

    alter table app2history
        add constraint FKA9F47143151F4145
        foreign key (PAPPID)
        references PC1APP;

    alter table issue2subIssue
        add constraint FK2353CBF2C0D2BC71
        foreign key (PTESTISSUEID)
        references PC1TESTISSUE;

    alter table mm_anouncement_osType
        add constraint FK993C9081A7F49779
        foreign key (PANOUNCEMENTID)
        references PC1ANOUNCEMENT;

    alter table mm_anouncement_osType
        add constraint FK993C90811F8C6498
        foreign key (POSTYPID)
        references PF1OSTYP;

    alter table mm_appHistory_apppack
        add constraint FK193447ADD74CA932
        foreign key (PAPPPACKID)
        references PC1PACKAGE;

    alter table mm_appHistory_apppack
        add constraint FK193447AD836141B9
        foreign key (PAPPHISTORYID)
        references PC1APPHISTORY;

    alter table mm_pack2TestIssue
        add constraint FK80F7AADC0D2BC71
        foreign key (PTESTISSUEID)
        references PC1TESTISSUE;

    alter table mm_testIssueHist_subIssue
        add constraint FK6EA7C84EF223542C
        foreign key (PTSTSUBISSUEID)
        references PC1TESTSUBISSUE;

    alter table mm_testIssueHist_subIssue
        add constraint FK6EA7C84EE3911D0D
        foreign key (PTESTISSUEHISTORYID)
        references PC1TESTISSUEHISTORY;

    alter table package2history
        add constraint FKD623668D74CA932
        foreign key (PAPPPACKID)
        references PC1PACKAGE;

    alter table package2history
        add constraint FKD6236682918852C
        foreign key (PAPPPACKHISTORYID)
        references PC1PACKAGEHISTORY;

    alter table testIssue2history
        add constraint FK4246D089C0D2BC71
        foreign key (PTESTISSUEID)
        references PC1TESTISSUE;

    alter table testIssue2history
        add constraint FK4246D089E3911D0D
        foreign key (PTESTISSUEHISTORYID)
        references PC1TESTISSUEHISTORY;

    create sequence SEQ_ANOUNCEMENT start with 1000 increment by 1;

    create sequence SEQ_APPHISTORY start with 1000 increment by 1;

    create sequence SEQ_APP_INSTALL_QUEUE start with 1000 increment by 1;

    create sequence SEQ_APP_PACK_HISTORY start with 1000 increment by 1;

    create sequence SEQ_HANDLERAPP start with 1000 increment by 1;

    create sequence SEQ_TESTISSUEHISTORY start with 1000 increment by 1;


    alter table PC1APP
        add PHASSCHEDULER number(1,0)
;
    create table PF1PACKAGEPUBLISH (
        PPACKAGEPUBLISHID number(19,0) not null,
        PCREATIONDT number(10,0),
        PCREATIONTM number(10,0),
        PHBCREATIONDT number(10,0),
        PHBCREATIONTM number(10,0),
        PHBLASTMDFDT number(10,0),
        PHBLASTMDFTM number(10,0),
        PLASTMDFDT number(10,0),
        PLASTMDFTM number(10,0),
        PHBVER number(19,0),
        PAPPID number(19,0),
        PISAPPLIED number(1,0),
        PPACKID number(19,0),
        PPUBLISHDT number(10,0),
        PPUBLISHTM number(10,0),
        CREATOR number(19,0),
        PHBCREATOR number(19,0),
        PHBLASTMDFUSR number(19,0),
        LASTMDFUSER number(19,0),
        primary key (PPACKAGEPUBLISHID)
    )
;
    create table mm_subIssue2device (
        PTSTSUBISSUEID number(19,0) not null,
        PDEVICEID number(19,0) not null
    )
;
    create table mm_testGroup2user (
        PTSTSUBISSUEID number(19,0) not null,
        PDEVICEID number(19,0) not null
    )
;
    alter table PC1TESTGROUP
        add constraint FK819DEA2F1C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PC1TESTGROUP
        add constraint FK819DEA2FFB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PC1TESTGROUP
        add constraint FK819DEA2FDBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PC1TESTGROUP
        add constraint FK819DEA2FD6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    alter table PC1TESTISSUE
        add constraint FK81BA9C891C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PC1TESTISSUE
        add constraint FK81BA9C89FB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PC1TESTISSUE
        add constraint FK81BA9C89DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PC1TESTISSUE
        add constraint FK81BA9C89D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    alter table PC1TESTSUBISSUE
        add constraint FKE2284C91C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PC1TESTSUBISSUE
        add constraint FKE2284C9FB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PC1TESTSUBISSUE
        add constraint FKE2284C9DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PC1TESTSUBISSUE
        add constraint FKE2284C957D86800
        foreign key (PTESTUSER)
        references PC1USER
;
    alter table PC1TESTSUBISSUE
        add constraint FKE2284C9D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    alter table PF1PACKAGEPUBLISH
        add constraint FK1F1F73C41C8DAD7
        foreign key (LASTMDFUSER)
        references PC1USER
;
    alter table PF1PACKAGEPUBLISH
        add constraint FK1F1F73C4FB81C05F
        foreign key (CREATOR)
        references PC1USER
;
    alter table PF1PACKAGEPUBLISH
        add constraint FK1F1F73C4DBA383B5
        foreign key (PHBCREATOR)
        references PC1USER
;
    alter table PF1PACKAGEPUBLISH
        add constraint FK1F1F73C4D6530CB8
        foreign key (PHBLASTMDFUSR)
        references PC1USER
;
    alter table issue2subIssue
        add constraint FK2353CBF2F223542C
        foreign key (PTSTSUBISSUEID)
        references PC1TESTSUBISSUE
;
    alter table mm_pack2TestIssue
        add constraint FK80F7AADD74CA932
        foreign key (PAPPPACKID)
        references PC1PACKAGE;

    alter table mm_subIssue2device
        add constraint FK3B8AE7D0B1771F
        foreign key (PDEVICEID)
        references PC1DEVICE
;
    alter table mm_subIssue2device
        add constraint FK3B8AE7D0F223542C
        foreign key (PTSTSUBISSUEID)
        references PC1TESTSUBISSUE
;
    alter table mm_testGroup2user
        add constraint FKDB44428FDE40BE94
        foreign key (PDEVICEID)
        references PC1USER
;
    alter table mm_testGroup2user
        add constraint FKDB44428FF2EA083E
        foreign key (PTSTSUBISSUEID)
        references PC1TESTGROUP
;
    alter table package2group
        add constraint FK2655AD5354FC863D
        foreign key (PTESTGROUPID)
        references PC1TESTGROUP
;
    create sequence SEQ_PACKAGEPUBLISH start with 1000 increment by 1;

    create sequence SEQ_TESTGROUP start with 1000 increment by 1;

    create sequence SEQ_TESTISSUE start with 1000 increment by 1;

    create sequence SEQ_TESTSUBISSUE start with 1000 increment by 1;
