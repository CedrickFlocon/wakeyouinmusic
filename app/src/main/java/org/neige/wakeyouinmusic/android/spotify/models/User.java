package org.neige.wakeyouinmusic.android.spotify.models;

import java.util.List;

public class User extends UserSimple {

	public static final String PRODUCT_PREMIUM = "premium";

    public String display_name;
    public String email;
    public Followers followers;
    public String country;
    public List<Image> images;
    public String product;
}
