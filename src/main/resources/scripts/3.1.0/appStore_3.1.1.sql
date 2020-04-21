-- hjk date 13990202
update PC1APP set PPACKNAME='wrong' where PPACKNAME is null;
alter table PC1APP
        add PPACKNAME varchar2(255 char) not null;