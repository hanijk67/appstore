-- date 13970601
    alter table PC1PACKAGEHISTORY
        add PICONFILEPATH varchar2(255 char) ;

    alter table PC1PACKAGEHISTORY
        add PPACKFILEPATH varchar2(255 char) ;

    create table appHistory2ThumbFiles (
        PAPPPACKID number(19,0) not null,
        PFILEID number(19,0) not null
    ) ;

    alter table appHistory2ThumbFiles
        add constraint FK6E371B0055CE026C
        foreign key (PAPPPACKID)
        references PC1PACKAGEHISTORY ;

    alter table appHistory2ThumbFiles
        add constraint FK6E371B0096EEC9EB
        foreign key (PFILEID)
        references PC1FILE ;