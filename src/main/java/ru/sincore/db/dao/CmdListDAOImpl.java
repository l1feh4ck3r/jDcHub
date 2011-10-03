package ru.sincore.db.dao;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import ru.sincore.db.HibernateUtils;
import ru.sincore.db.pojo.CmdListPOJO;

import java.util.List;

/**
 *  @author Valor
 *  @since 14.09.2011
 *  @version 0.0.1
 */
public class CmdListDAOImpl implements CmdListDAO
{
	private static final Logger log = LoggerFactory.getLogger(CmdListDAOImpl.class);

	private static String marker = Marker.ANY_MARKER;

	@Override
	public boolean addCommand(String name,
							  int weight,
							  String executorClass,
							  String args,
							  String description,
							  String syntax,
							  Boolean enabled,
							  Boolean logged)
	{
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();

		try{

			tx.begin();

			CmdListPOJO pojo = new CmdListPOJO();

			pojo.setCommandName(name);
			pojo.setCommandWeight(weight);
			pojo.setCommandExecutorClass(executorClass);
			pojo.setCommandArgs(args);
			pojo.setCommandDescription(description);
			pojo.setCommandSyntax(syntax);
			pojo.setEnabled(enabled);
			pojo.setLogged(logged);

			session.save(pojo);
			tx.commit();

			if (log.isDebugEnabled())
			{
				log.debug(marker,"Command  :"+name+" stored.");
			}

			return true;

		}catch (Exception ex)
		{
			tx.rollback();
			log.error(marker, ex);
		}

		return false;
	}

	@Override
	public boolean delCommand(String name)
	{
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();

		try{

			tx.begin();

			Query query = session.createQuery("delete from CmdListPOJO where commandName = :commandName").setParameter("commandName",name);

			query.executeUpdate();

			tx.commit();

			if(log.isDebugEnabled())
			{
				log.debug(marker,"Command : "+name+" deleted");
			}

			return true;

		}catch (Exception ex)
		{
			tx.rollback();
			log.error(marker, ex);
		}
		return false;
	}

	@Override
	public boolean updateCommand(CmdListPOJO commandObject)
	{
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();

		try{

			tx.begin();
			session.update(commandObject);
			tx.commit();

			if (log.isDebugEnabled())
			{
				log.debug(marker,"Command : "+commandObject.getCommandName()+" updated");
			}

			return true;

		}catch (Exception ex)
		{
			tx.rollback();
			log.error(marker, ex);
		}

		return false;
	}

	@Override
	public List<CmdListPOJO> getCommandList()
	{
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();

		try{

			tx.begin();

			Query query = session.createQuery("from CmdListPOJO");

			List<CmdListPOJO> resultList = (List<CmdListPOJO>) query.list();

			tx.commit();

			return resultList;

		}catch (HibernateException ex)
		{
			tx.rollback();
			log.error(marker, ex);
		}

		return null;
	}

	public CmdListPOJO getCommandInfo(String command)
	{
		Session session = HibernateUtils.getSessionFactory().openSession();
		Transaction tx = session.getTransaction();

		try{

			tx.begin();

			Query query = session.createQuery("from CmdListPOJO where commandName =:cmd");

			query.setParameter("cmd",command);

			CmdListPOJO result = (CmdListPOJO) query.uniqueResult();

			tx.commit();

			return result;

		}catch (HibernateException ex)
		{
			tx.rollback();
			log.error(marker, ex);
		}

		return null;
	}
}
