package com.driver;

import java.util.*;

import org.springframework.stereotype.Repository;

@Repository
public class SpotifyRepository {
    public HashMap<Artist, List<Album>> artistAlbumMap;
    public HashMap<Album, List<Song>> albumSongMap;
    public HashMap<Playlist, List<Song>> playlistSongMap;
    public HashMap<Playlist, List<User>> playlistListenerMap;
    public HashMap<User, Playlist> creatorPlaylistMap;
    public HashMap<User, List<Playlist>> userPlaylistMap;
    public HashMap<Song, List<User>> songLikeMap;

    public List<User> users;
    public List<Song> songs;
    public List<Playlist> playlists;
    public List<Album> albums;
    public List<Artist> artists;

    public SpotifyRepository() {
        //To avoid hitting apis multiple times, initialize all the hashmaps here with some dummy data
        artistAlbumMap = new HashMap<>();
        albumSongMap = new HashMap<>();
        playlistSongMap = new HashMap<>();
        playlistListenerMap = new HashMap<>();
        creatorPlaylistMap = new HashMap<>();
        userPlaylistMap = new HashMap<>();
        songLikeMap = new HashMap<>();

        users = new ArrayList<>();
        songs = new ArrayList<>();
        playlists = new ArrayList<>();
        albums = new ArrayList<>();
        artists = new ArrayList<>();
    }

    public User createUser(String name, String mobile) {
        User user = new User(name, mobile);
        users.add(user);
        return user;
    }

    public Artist createArtist(String name) {
        Artist artist = new Artist(name);
        artists.add(artist);
        return artist;
    }

    public Album createAlbum(String title, String artistName) {

        Artist artist = getArtist(artistName);
        if (artist == null)
            createArtist(artistName);

        Album album = new Album(title);
        albums.add(album);

        List<Album> albumList = artistAlbumMap.getOrDefault(artist, new ArrayList<>());
        albumList.add(album);
        artistAlbumMap.put(artist, albumList);

        return album;
    }


    public Song createSong(String title, String albumName, int length) throws Exception {
        Album album = getAlbum(albumName);
        if (!albums.contains(new Album((albumName))))
            throw new Exception("Album does not exist");

        Song song = new Song(title, length);
        songs.add(song);

        List<Song> songLists = albumSongMap.getOrDefault(album, new ArrayList<>());
        songLists.add(song);
        albumSongMap.put(album, songLists);

        return song;
    }

    public Playlist createPlaylistOnLength(String mobile, String title, int length) throws Exception {
        User user = getUserByMobileNumber(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> allSongs = new ArrayList<>();
        for (Song song : songs) {
            if (song.getLength() == length) {
                allSongs.add(song);
            }
        }

        playlistSongMap.put(playlist, allSongs);

        List<User> userList = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        userList.add(user);
        playlistListenerMap.put(playlist, userList);

        creatorPlaylistMap.put(user, playlist);

        List<Playlist> playlistsData = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistsData.add(playlist);
        userPlaylistMap.put(user, playlistsData);

        return playlist;
    }

    public Playlist createPlaylistOnName(String mobile, String title, List<String> songTitles) throws Exception {
        User user = getUserByMobileNumber(mobile);
        if (user == null)
            throw new Exception("User does not exist");

        Playlist playlist = new Playlist(title);
        playlists.add(playlist);

        List<Song> allSongs = new ArrayList<>();
        for (Song song : songs) {
            if (songTitles.contains(song.getTitle())) {
                allSongs.add(song);
            }
        }

        playlistSongMap.put(playlist, allSongs);

        List<User> userList = playlistListenerMap.getOrDefault(playlist, new ArrayList<>());
        userList.add(user);
        playlistListenerMap.put(playlist, userList);

        creatorPlaylistMap.put(user, playlist);

        List<Playlist> playlistsData = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        playlistsData.add(playlist);
        userPlaylistMap.put(user, playlistsData);

        return playlist;
    }

    public Playlist findPlaylist(String mobile, String playlistTitle) throws Exception {
        User user = getUserByMobileNumber(mobile);
        Playlist playlist = getPlayListByPlayListTitle(playlistTitle);

        if (user == null)
            throw new Exception("User does not exist");

        if (playlist == null)
            throw new Exception("Playlist does not exist");

        if (creatorPlaylistMap.containsKey(user))
            return playlist;

        List<User> listiner = playlistListenerMap.get(playlist);
        for (User listiner_user : listiner) {
            if (listiner_user == user)
                return playlist;
        }

        listiner.add(user);
        playlistListenerMap.put(playlist, listiner);

        List<Playlist> allPlaylists = userPlaylistMap.getOrDefault(user, new ArrayList<>());
        if (allPlaylists.contains(playlist))
            return playlist;

        allPlaylists.add(playlist);

        return playlist;
    }

    public Song likeSong(String mobile, String songTitle) throws Exception {
        User user = getUserByMobileNumber(mobile);
        Song song = getSongBySongTitle(songTitle);

        if (user == null)
            throw new Exception("User does not exist");

        if (song == null)
            throw new Exception("Song does not exist");

        List<User> userList = songLikeMap.getOrDefault(song, new ArrayList<>());
        if (userList.contains(user))
            return song;

        userList.add(user);
        song.setLikes(song.getLikes() + 1);
        songLikeMap.put(song, userList);

        Album album = null;
        for (Album album1 : albumSongMap.keySet()) {
            if (albumSongMap.get(album1).contains(song)) {
                album = album1;
                break;
            }
        }

        Artist artist = null;
        for (Artist artist1 : artistAlbumMap.keySet()) {
            if (artistAlbumMap.get(artist1).contains(album)) {
                artist = artist1;
                break;
            }
        }

        if (artist != null) {
            artist.setLikes(artist.getLikes() + 1);
            artists.add(artist);
        }

        return song;
    }

    public String mostPopularArtist() {
        int max = 0;
        Artist artist1 = null;

        for (Artist artist : artists) {
            if (artist.getLikes() >= max) {
                artist1 = artist;
                max = artist.getLikes();
            }
        }
        if (artist1 == null)
            return null;
        else
            return artist1.getName();
    }

    public String mostPopularSong() {
        int max = 0;
        Song song = null;

        for (Song song1 : songLikeMap.keySet()) {
            if (song1.getLikes() >= max) {
                song = song1;
                max = song1.getLikes();
            }
        }
        if (song == null)
            return null;
        else
            return song.getTitle();
    }

    private Artist getArtist(String artistName) {
        for (Artist artist : artists) {
            if (artistName.equals(artist.getName()))
                return artist;
        }
        return null;
    }

    private Album getAlbum(String albumName) {
        for (Album album : albums) {
            if (albumName.equals(album.getTitle()))
                return album;
        }
        return null;
    }

    private User getUserByMobileNumber(String mobile) {
        for (User user : users) {
            if (mobile.equals(user.getMobile()))
                return user;
        }
        return null;
    }

    private Playlist getPlayListByPlayListTitle(String playlistTitle) {
        for (Playlist playlist : playlists) {
            if (playlist.getTitle().equals(playlistTitle))
                return playlist;
        }
        return null;
    }

    private Song getSongBySongTitle(String songTitle) {
        for (Song song : songs) {
            if (song.getTitle().equals(songTitle))
                return song;
        }
        return null;
    }
}
