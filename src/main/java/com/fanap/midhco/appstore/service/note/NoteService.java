package com.fanap.midhco.appstore.service.note;

/**
 * Created by admin123 on 12/14/2016.
 */
import com.fanap.midhco.appstore.entities.Note;
import com.fanap.midhco.appstore.entities.User;
import com.fanap.midhco.appstore.entities.helperClasses.DateTime;
import com.fanap.midhco.appstore.service.HQLBuilder;
import com.fanap.midhco.appstore.service.HibernateUtil;
import com.fanap.midhco.ui.access.PrincipalUtil;
import org.hibernate.Query;
import org.hibernate.Session;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;

public class NoteService {
    public static NoteService Instance = new NoteService();

    private NoteService() {
    }

    public Note load(Serializable id, Session session) {
        return (Note) session.load(Note.class, id);
    }

    public static class NoteCriteria implements Serializable {
        public DateTime[] creationDate = new DateTime[2];
        public String noteText;
        public String noteParentEntity;
        public Collection<User> creatorUser;
        public String noteFieldName;
        public Long parentEntityId;
    }

    private void applyNoteCriteria(HQLBuilder builder, NoteCriteria criteria) {
        if (criteria.creationDate != null)
            builder.addDateTimeRange("note", "creationDate", "lcreatedDateTime", "ucreatedDateTime", criteria.creationDate);

        if (criteria.noteText != null && !criteria.noteText.equals(""))
            builder.addClause("and note.noteText like (:noteText_)", "noteText_", HQLBuilder.like(criteria.noteText));

        if (criteria.creatorUser != null && !criteria.creatorUser.isEmpty())
            builder.addClause("and ent.creatorUser in (:creatorUser_)", "creatorUser_", criteria.creatorUser);

    }


    public Long count(NoteCriteria cri, String sortProp, boolean isAscending) {
        Session session = HibernateUtil.getCurrentSession();

        StringBuilder fromClause =
                new StringBuilder(" from ").append(cri.noteParentEntity).append(" noteParent ")
                        .append(", Note note ");

        HQLBuilder builder = new HQLBuilder(session, "select count(note.code) ", fromClause.toString());

        if (cri.parentEntityId != null)
            builder.addClause("and noteParent.code = :parentEntityCode_", "parentEntityCode_", cri.parentEntityId);

        builder.addClause("and note in elements(noteParent." + cri.noteFieldName + ")");

        applyNoteCriteria(builder, cri);

        Query query = builder.createQuery();
        return (Long) query.uniqueResult();
    }


    public List<Note> list(NoteCriteria cri, int first, int count, String sortProp, boolean isAscending) {
        Session session = HibernateUtil.getCurrentSession();

        StringBuilder fromClause =
                new StringBuilder(" from ").append(cri.noteParentEntity).append(" noteParent ")
                        .append(", Note note ");

        HQLBuilder builder = new HQLBuilder(session, "select note ", fromClause.toString());

        if (cri.parentEntityId != null)
            builder.addClause("and noteParent.code = :parentEntityCode_", "parentEntityCode_", cri.parentEntityId);

        builder.addClause("and note in elements(noteParent." + cri.noteFieldName + ")");

        applyNoteCriteria(builder, cri);

        if (sortProp != null)
            builder.addOrder(sortProp, isAscending);

        Query query = builder.createQuery();
        query.setFirstResult(first);
        if (count != -1)
            query.setMaxResults(count);

        return query.list();
    }


    public void addNote(NoteCriteria noteCriteria, String noteText) throws Exception {
//        Class parentEntType = Class.forName(noteCriteria.noteParentEntity);

        Session session = HibernateUtil.getCurrentSession();
        StringBuilder queryBuilder = new StringBuilder("select parentEnt from ")
                .append(noteCriteria.noteParentEntity).append(" parentEnt ")
                .append("where parentEnt.code = :parentEntCode_");
        Query query = session.createQuery(queryBuilder.toString());
        query.setParameter("parentEntCode_", noteCriteria.parentEntityId);


        Object parentEnt = query.list().get(0);

        Field notesField = parentEnt.getClass().getField("notes");
        notesField.setAccessible(true);
        List<Note> notes = (List) notesField.get(parentEnt);

        Note noteToAdd = new Note();
        noteToAdd.setCreationDate(DateTime.now());
        noteToAdd.setCreatorUser(PrincipalUtil.getCurrentUser());
        noteToAdd.setNoteText(noteText);

        session.beginTransaction();
        session.save(noteToAdd);
        notes.add(noteToAdd);
        session.saveOrUpdate(parentEnt);
    }

    public void saveOrUpdate(Note note) {
        Session session = HibernateUtil.getCurrentSession();
        if(note.getId() == null) {
            note.setCreatorUser(PrincipalUtil.getCurrentUser());
            note.setCreationDate(DateTime.now());
        }
        session.beginTransaction();
        session.saveOrUpdate(note);
    }


    public static void main(String[] args) throws Exception {
        HibernateUtil.getCurrentSession().beginTransaction();

        NoteCriteria criteria = new NoteCriteria();
        criteria.noteParentEntity = "ChangeAccountRequest";
        criteria.noteFieldName = "notes";
        criteria.parentEntityId = 21L;

        NoteService.Instance.addNote(criteria, "salam sag");

        HibernateUtil.endTransaction();
    }
}
