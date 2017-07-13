package org.vaadin.patrik.demo;

import java.util.Date;
import java.util.Random;

public class DemoColumns
{
	private String col1;
	private String col2;
	private Integer col3;
	private Integer col4;
	private Integer col5;
	private Integer col6;
	private Integer col7;
	private Date col8;
	private Boolean col10;
	private String col11;
	
	Random rand = new Random();
	
	DemoColumns(int row)
	{
        col1 = "string 1 " + row;
        col2 = "string 2 " + row;
        col3 = rand.nextInt(row + 10);
        col4 = rand.nextInt(row + 10);
        col5 = rand.nextInt(row + 10);
        col6 = rand.nextInt(row + 10);
        col7 = rand.nextInt(row + 10);
        col8 = new Date();
        col10 = false;
        col11 = "Medium";

	}
	
	
	public String getCol1()
	{
		return col1;
	}
	public void setCol1(String col1)
	{
		this.col1 = col1;
	}
	public String getCol2()
	{
		return col2;
	}
	public void setCol2(String col2)
	{
		this.col2 = col2;
	}
	public Integer getCol3()
	{
		return col3;
	}
	public void setCol3(Integer col3)
	{
		this.col3 = col3;
	}
	public Integer getCol4()
	{
		return col4;
	}
	public void setCol4(Integer col4)
	{
		this.col4 = col4;
	}
	public Integer getCol5()
	{
		return col5;
	}
	public void setCol5(Integer col5)
	{
		this.col5 = col5;
	}
	public Integer getCol6()
	{
		return col6;
	}
	public void setCol6(Integer col6)
	{
		this.col6 = col6;
	}
	public Integer getCol7()
	{
		return col7;
	}
	public void setCol7(Integer col7)
	{
		this.col7 = col7;
	}
	public Date getCol8()
	{
		return col8;
	}
	public void setCol8(Date col8)
	{
		this.col8 = col8;
	}
	public Boolean getCol10()
	{
		return col10;
	}
	public void setCol10(Boolean col10)
	{
		this.col10 = col10;
	}
	public String getCol11()
	{
		return col11;
	}
	public void setCol11(String col11)
	{
		this.col11 = col11;
	}


	@Override
	public String toString()
	{
		return "DemoColumns [col1=" + col1 + ", col2=" + col2 + ", col3=" + col3 + ", col4=" + col4 + ", col5=" + col5
				+ ", col6=" + col6 + ", col7=" + col7 + ", col8=" + col8 + ", col10=" + col10 + ", col11=" + col11
				+ ", rand=" + rand + "]";
	}
	
}
