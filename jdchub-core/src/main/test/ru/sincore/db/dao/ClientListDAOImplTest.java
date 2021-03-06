package ru.sincore.db.dao;

import org.apache.log4j.PropertyConfigurator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.sincore.ConfigurationManager;
import ru.sincore.db.pojo.ClientListPOJO;

import java.util.List;

public class ClientListDAOImplTest
{
	@BeforeMethod
	public void setUp() throws Exception
	{
		PropertyConfigurator.configure(ConfigurationManager.getInstance().getHubConfigDir() + "/log4j.properties");
	}

	@Test
	public void testAddClient() throws Exception
	{


		ClientListDAOImpl clientList = new ClientListDAOImpl();

		ClientListPOJO client = new ClientListPOJO();
		client.setCid("UTFFD#@");
		client.setCurrentIp("10.10.10.125");
		client.setLastNick("Valor");
		client.setNickName("Valor");
		client.setPassword("123456");


		clientList.addClient(client);
	}

	@Test
	public void testDelClient() throws Exception
	{

	}

	@Test
	public void testGetClientList() throws Exception
	{
		ClientListDAOImpl clientList = new ClientListDAOImpl();
		List<ClientListPOJO> clients = clientList.getClientList(false);

		for(ClientListPOJO list : clients)
		{
			System.out.println(">>> "+list.getCid());
		}
	}

	@Test
	public void testUpdateClient() throws Exception
	{

	}
}
