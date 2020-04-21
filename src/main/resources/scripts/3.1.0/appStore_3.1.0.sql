    alter table PF1HANDLERAPP
        add PFILEID32BIT number(19,0);

    alter table PF1HANDLERAPP
        add PTESTFILEID32BIT number(19,0);

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D289D29C8B
        foreign key (PFILEID32BIT)
        references PC1FILE;

    alter table PF1HANDLERAPP
        add constraint FKB6B9C9D25132E0B9
        foreign key (PTESTFILEID32BIT)
        references PC1FILE
;