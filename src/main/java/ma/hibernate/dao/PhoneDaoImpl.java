package ma.hibernate.dao;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import ma.hibernate.exception.DataProcessingException;
import ma.hibernate.model.Phone;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

public class PhoneDaoImpl extends AbstractDao implements PhoneDao {
    public PhoneDaoImpl(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public Phone create(Phone phone) {
        Session session = null;
        Transaction transaction = null;

        try {
            session = factory.openSession();
            transaction = session.beginTransaction();

            session.persist(phone);
            transaction.commit();
            return phone;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't create phone " + phone, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Override
    public List<Phone> findAll(Map<String, String[]> params) {
        try (Session session = factory.openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<Phone> getAllPhonesWithParamsCriteriaQuery = cb.createQuery(Phone.class);
            Root<Phone> phoneRoot = getAllPhonesWithParamsCriteriaQuery.from(Phone.class);

            List<Predicate> predicateList = new ArrayList<>();

            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                CriteriaBuilder.In<String> paramPredicate = cb.in(phoneRoot.get(entry.getKey()));
                for (String e : entry.getValue()) {
                    paramPredicate.value(e);
                }
                predicateList.add(paramPredicate);
            }

            getAllPhonesWithParamsCriteriaQuery.where(predicateList.toArray(new Predicate[0]));
            return session.createQuery(getAllPhonesWithParamsCriteriaQuery).getResultList();

        } catch (Exception e) {
            throw new DataProcessingException("Failed to retrieve phones", e);
        }
    }
}
