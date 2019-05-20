package api;

import model.ImageResponse;
import okhttp3.MultipartBody;
import retrofit2.Call;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface HeroAPI {
@FormUrlEncoded
    @POST("heroes")
    Call<Void> addHero(@Field("name") String name, @Field(("desc")) String desc, @Field("image") String image);

@Multipart
    @POST("upload")
    Call<ImageResponse> uploadImage(@Part MultipartBody.Part img);
}

