package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import db.MySQLConnection;
import entity.Item;
import entity.Item.ItemBuilder;
import external.TicketMasterClient;

/**
 * Servlet implementation class ItemHistory
 */
@WebServlet("/history")
public class ItemHistory extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ItemHistory() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
//		HttpSession session = request.getSession(false);
//		if (session == null) {
//			response.setStatus(403);
//			return;
//		}
//		
//		String userId = session.getAttribute("user_id").toString();
//		// String userId = request.getParameter("user_id");
//		double lat = Double.parseDouble(request.getParameter("lat"));
//		double lon = Double.parseDouble(request.getParameter("lon"));
//		String term = request.getParameter("term");
//
//		TicketMasterClient client = new TicketMasterClient();
//		List<Item> items = client.search(lat, lon, null);
//
//
//		MySQLConnection connection = new MySQLConnection();
//		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
//		connection.close();
//
//		JSONArray array = new JSONArray();
//		for (Item item : items) {
//			JSONObject obj = item.toJSONObject();
//			try {
//				obj.put("favorite", favoritedItemIds.contains(item.getItemId()));
//			} catch (JSONException e) {
//				e.printStackTrace();
//			}
//			array.put(obj);
//		}
//		RpcHelper.writeJsonArray(response, array);
    	String userId = request.getParameter("user_id");
		JSONArray array = new JSONArray();

		MySQLConnection connection = new MySQLConnection();
		Set<Item> items = connection.getFavoriteItems(userId);
		connection.close();

		for (Item item : items) {
			JSONObject obj = item.toJSONObject();
			try {
				obj.append("favorite", true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			array.put(obj);
		}
		RpcHelper.writeJsonArray(response, array);


	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject input = RpcHelper.readJSONObject(request);
		try {
			String userId = input.getString("user_id");
			Item item = RpcHelper.parseFavoriteItem(input.getJSONObject("favorite"));

			MySQLConnection connection = new MySQLConnection();
			connection.setFavoriteItems(userId, item);
			connection.close();
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		JSONObject input = RpcHelper.readJSONObject(request);
		try {
			String userId = input.getString("user_id");
			String itemId = input.getJSONObject("favorite").getString("item_id");

			MySQLConnection connection = new MySQLConnection();
			connection.unsetFavoriteItems(userId, itemId);
			connection.close();
			RpcHelper.writeJsonObject(response, new JSONObject().put("result", "SUCCESS"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	// Parses a JSONObject from http request.
		public static JSONObject readJSONObject(HttpServletRequest request) {
			StringBuilder sBuilder = new StringBuilder();
			try (BufferedReader reader = request.getReader()) {
				String line = null;
				while ((line = reader.readLine()) != null) {
					sBuilder.append(line);
				}
				return new JSONObject(sBuilder.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}

			return new JSONObject();
		}
		
		public static Item parseFavoriteItem(JSONObject favoriteItem) throws JSONException {
			ItemBuilder builder = new ItemBuilder();
			builder.setItemId(favoriteItem.getString("item_id"));
			builder.setName(favoriteItem.getString("name"));
			builder.setRating(favoriteItem.getDouble("rating"));
			builder.setDistance(favoriteItem.getDouble("distance"));
			builder.setImageUrl(favoriteItem.getString("image_url"));
			builder.setUrl(favoriteItem.getString("url"));
			builder.setAddress(favoriteItem.getString("address"));

			Set<String> categories = new HashSet<>();
			if (!favoriteItem.isNull("categories")) {
				JSONArray array = favoriteItem.getJSONArray("categories");
				for (int i = 0; i < array.length(); ++i) {
					categories.add(array.getString(i));
				}
			}
			builder.setCategories(categories);
			return builder.build();
		}


	

}
