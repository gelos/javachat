package chat.client.mvp.presenter;

public final class PresenterFabric {

  public static Presenter createPresenter() {
    return new ClientPresenterNew();
    //return new ClientPresenter();
  }
  
}
