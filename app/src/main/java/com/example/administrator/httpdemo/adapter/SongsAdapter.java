package com.example.administrator.httpdemo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.httpdemo.CustomView.CustomDialog;
import com.example.administrator.httpdemo.CustomView.CustomDialogAdapter;
import com.example.administrator.httpdemo.Data.entity.CreateSongList;
import com.example.administrator.httpdemo.Data.entity.Song;
import com.example.administrator.httpdemo.Data.entity.Song2;
import com.example.administrator.httpdemo.Data.remote.BaseNetData;
import com.example.administrator.httpdemo.Listener.HttpListener;
import com.example.administrator.httpdemo.Listener.ListDialogListener;
import com.example.administrator.httpdemo.MusicApp;
import com.example.administrator.httpdemo.Other.Constant;
import com.example.administrator.httpdemo.R;
import com.example.administrator.httpdemo.Utils.OtherUtils;
import com.example.administrator.httpdemo.Utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.UpdateListener;
import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by Administrator on 2017/8/9.
 */

public class SongsAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<Song2> mSongs;
    private CustomDialog mDialog;

    public SongsAdapter(Context context, List<Song2> songs) {
        mContext = context;
        mSongs = songs;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mSongs.size();
    }

    @Override
    public Object getItem(int position) {
        return mSongs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if(convertView == null){
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_song, null);
            viewHolder.iv_action = (ImageView) convertView.findViewById(R.id.action);
            viewHolder.iv_trumpet = (ImageView) convertView.findViewById(R.id.trumpet);
            viewHolder.tv_singer = (TextView) convertView.findViewById(R.id.singer);
            viewHolder.tv_position = (TextView) convertView.findViewById(R.id.position);
            viewHolder.tv_songName = (TextView) convertView.findViewById(R.id.songName);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        String name = mSongs.get(position).getTitle();
        String singer = mSongs.get(position).getAuthor();

        viewHolder.tv_singer.setText(singer);
        viewHolder.tv_songName.setText(name);
        viewHolder.tv_position.setText(String.valueOf(position+1));
        viewHolder.tv_position.setVisibility(View.VISIBLE);
        viewHolder.iv_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog = CustomDialogAdapter.createDialog(mContext, "收藏到歌单", new ListDialogListener() {
                    @Override
                    public void createListDialog(final ListView listView) {
                        new BmobQuery<CreateSongList>().addWhereEqualTo("userID", OtherUtils.getUserId()).findObjects(new FindListener<CreateSongList>() {
                            @Override
                            public void done(final List<CreateSongList> list, BmobException e) {
                                if (e == null) {
                                    listView.setAdapter(new CreateSongListAdapter(mContext, list));
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, final int pos, final long id) {
                                            CreateSongList createSongList = list.get(pos);
                                            BmobQuery<CreateSongList> bmobQuery = new BmobQuery<>();
                                            bmobQuery.getObject(createSongList.getObjectId(), new QueryListener<CreateSongList>() {
                                                @Override
                                                public void done(CreateSongList createSongList, BmobException e) {
                                                    if (e == null) {
                                                        List<String> ids = createSongList.getNetSongIds();
                                                        if (ids == null) {
                                                            ids = new ArrayList<>();
                                                        }
                                                        ids.add(mSongs.get(position).getSong_id());
                                                        createSongList.setNetSongIds(ids);
                                                        createSongList.update(createSongList.getObjectId(), new UpdateListener() {
                                                            @Override
                                                            public void done(BmobException e) {
                                                                if (e != null) {
                                                                    ToastUtils.showShort(mContext, "收藏失败");
                                                                } else {
                                                                    ToastUtils.showShort(mContext, "收藏成功");
                                                                    EventBus.getDefault().postSticky(1);
                                                                }
                                                            }
                                                        });
                                                    }
                                                }
                                            });
                                            mDialog.dismiss();
                                        }
                                    });
                                }
                            }
                        });

                    }
                });

                mDialog.setGravity(Gravity.CENTER).setDefaultContentHeight(mDialog.getDefaultContentHeight()).build().show();
            }
        });
        return convertView;
    }

    private class ViewHolder{
        TextView tv_songName;
        TextView tv_singer;
        ImageView iv_action;
        TextView tv_position;
        ImageView iv_trumpet;
    }
}
