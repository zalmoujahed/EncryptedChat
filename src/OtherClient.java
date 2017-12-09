import java.math.BigInteger;

public class OtherClient {
	
	int ID;
	BigInteger E; 
	BigInteger N;
	
	
	public OtherClient(String id, String e, String n){
		
		ID = Integer.parseInt(id);
		E = new BigInteger(e);
		N = new BigInteger(n);
		
	}
	
	public void setE(String e)
	{
		E = new BigInteger(e);
	}
	
	public void setN(String n)
	{
		N = new BigInteger(n);
	}
	
<<<<<<< HEAD
	public String getID()
=======
	public void setID(String id)
>>>>>>> 380abbbbabf73a3d436780005d2176e404e9f8e6
	{
		ID = Integer.parseInt(id);
	}

	public BigInteger getE()
	{
		return E;
	}
	
	public BigInteger getN()
	{
		return N;
	}
	
	public int getID()
	{
		return ID;
	}
}
