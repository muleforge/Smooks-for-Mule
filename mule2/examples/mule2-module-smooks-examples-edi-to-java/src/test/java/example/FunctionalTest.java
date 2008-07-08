package example;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import org.junit.Test;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.module.client.MuleClient;
import org.mule.tck.FunctionalTestCase;
import org.mule.util.IOUtils;

import example.model.Customer;
import example.model.Header;
import example.model.Order;
import example.model.OrderItem;

/**
 * Unit test for this example
 *
 * @author <a href="mailto:maurice@zeijen.net">Maurice Zeijen</a>
 *
 */
public class FunctionalTest extends FunctionalTestCase
{
	@Override
	protected String getConfigResources() {

		return "mule-edi-to-java-config.xml";
	}


	@Test
	public void testSmooks() throws Exception {
		InputStream in = IOUtils.getResourceAsStream("test-message01.edi", this.getClass());

		MuleClient client = new MuleClient();
		MuleMessage reply = client.send("vm://EDIOrderToJavaOrder",	new DefaultMuleMessage(in));

		assertNotNull(reply);
		assertNotNull(reply.getPayload());

		Object payload = reply.getPayload();

		assert payload instanceof Order : "The message payload is not an instance of Order";

		assertOrder((Order) payload);

		assert getReportFile().exists() : "The report file wasn't created";
	}

	private void assertOrder(Order order) {

		assertHeader(order.getHeader());
		assertOrderItems(order.getOrderItems());

	}


	private void assertHeader(Header header) {
		assert "1".equals(header.getOrderId());
		assert 0 == header.getOrderStatus();
		assert new BigDecimal("59.97").equals(header.getNetAmount());
		assert new BigDecimal("64.92").equals(header.getTotalAmount());
		assert new BigDecimal("4.95").equals(header.getTax());
		assert new GregorianCalendar(2006, 10, 15, 13, 45, 28).getTime().equals(header.getDate());

		assertCustomer(header.getCustomer());
	}

	private void assertCustomer(Customer customer) {

		assert "user1".equals(customer.getUserName());
		assert "Harry".equals(customer.getFirstName());
		assert "Fletcher".equals(customer.getLastName());
		assert "SD".equals(customer.getState());
	}

	/**
	 * @param orderItems
	 */
	private void assertOrderItems(List<OrderItem> orderItems) {

		assert 2 == orderItems.size();

		OrderItem orderItem = orderItems.get(0);

		assert 1 == orderItem.getQuantity();
		assert "364".equals(orderItem.getProductId());
		assert "The 40-Year-Old Virgin".equals(orderItem.getTitle());
		assert new BigDecimal("29.98").equals(orderItem.getPrice());

		orderItem = orderItems.get(1);

		assert 1 == orderItem.getQuantity();
		assert "299".equals(orderItem.getProductId());
		assert "Pulp Fiction".equals(orderItem.getTitle());
		assert new BigDecimal("29.99").equals(orderItem.getPrice());
	}


	private File getReportFile() {
		return new File("target/smooks-report/report.html");
	}

	private void deleteReportFile() {
		getReportFile().delete();
	}


	/* (non-Javadoc)
	 * @see org.mule.tck.AbstractMuleTestCase#doSetUp()
	 */
	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		TimeZone.setDefault(TimeZone.getTimeZone("EST"));
		Locale.setDefault(new Locale("en","IE"));
		deleteReportFile();
	}

	/* (non-Javadoc)
	 * @see org.mule.tck.AbstractMuleTestCase#doTearDown()
	 */
	@Override
	protected void doTearDown() throws Exception {
		super.doTearDown();

		deleteReportFile();
	}
}
